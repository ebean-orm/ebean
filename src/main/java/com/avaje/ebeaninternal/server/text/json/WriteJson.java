package com.avaje.ebeaninternal.server.text.json;

import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebean.text.PathProperties;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.deploy.BeanProperty;
import com.avaje.ebeaninternal.server.util.ArrayStack;
import com.fasterxml.jackson.core.JsonGenerator;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;

public class WriteJson {

  private final SpiEbeanServer server;
  
  private final JsonGenerator generator;
  
  private final PathProperties pathProperties;

  private final PathStack pathStack = new PathStack();

  private final ArrayStack<Object> parentBeans = new ArrayStack<Object>();

  public WriteJson(SpiEbeanServer server, JsonGenerator generator, PathProperties pathProperties){
    this.server = server;
    this.generator = generator;
    this.pathProperties = pathProperties;
  }

  public JsonGenerator gen() {
    return generator;
  }

  public boolean isParentBean(Object bean) {
    return !parentBeans.isEmpty() && parentBeans.contains(bean);
  }

  public void pushParentBeanMany(Object parentBean) {
    parentBeans.push(parentBean);
  }

  public void popParentBeanMany() {
    parentBeans.pop();
  }

  public void beginAssocOne(String key, Object bean) {
    parentBeans.push(bean);
    pathStack.pushPathKey(key);
  }

  public void endAssocOne() {
    parentBeans.pop();
    pathStack.pop();
  }

  public WriteBean createWriteBean(BeanDescriptor<?> desc, EntityBean bean) {

    if (pathProperties == null) {
      return new WriteBean(desc, bean);
    }
    
    boolean explicitAllProps = false;
    Set<String> currentIncludeProps = pathProperties.get(pathStack.peekWithNull());
    if (currentIncludeProps != null) {
      explicitAllProps = currentIncludeProps.contains("*");
      if (explicitAllProps || currentIncludeProps.isEmpty()) {
        currentIncludeProps = null;
      }
    }
    return new WriteBean(desc, explicitAllProps, currentIncludeProps, bean);
  }

  public static class WriteBean {
    
    final boolean explicitAllProps;
    final Set<String> currentIncludeProps;
    final BeanDescriptor<?> desc;
    final EntityBean currentBean;
    
    WriteBean(BeanDescriptor<?> desc, EntityBean currentBean){
      this(desc, false, null, currentBean);
    }

    WriteBean(BeanDescriptor<?> desc, boolean explicitAllProps, Set<String> currentIncludeProps, EntityBean currentBean) {
      super();
      this.desc = desc;
      this.currentBean = currentBean;
      this.explicitAllProps = explicitAllProps;
      this.currentIncludeProps = currentIncludeProps;
    }

    private boolean isReferenceOnly() {
      return !explicitAllProps && currentIncludeProps == null && currentBean._ebean_getIntercept().isReference();
    }

    private boolean isIncludeProperty(BeanProperty prop) {
      if (explicitAllProps)
        return true;
      if (currentIncludeProps != null) {
        // explicitly controlled by pathProperties
        return currentIncludeProps.contains(prop.getName());
      } else {
        // include only loaded properties
        return currentBean._ebean_getIntercept().isLoadedProperty(prop.getPropertyIndex());
      }
    }

    public void write(WriteJson writeJson) throws IOException {
      
      BeanProperty beanProp = desc.getIdProperty();
      if (beanProp != null) {
        if (isIncludeProperty(beanProp)) {
          beanProp.jsonWrite(writeJson, currentBean);
        }
      }
  
      if (!isReferenceOnly()) {
        // render all the properties and invoke lazy loading if required
        BeanProperty[] props = desc.propertiesNonTransient();
        for (int j = 0; j < props.length; j++) {
          if (isIncludeProperty(props[j])) {
            props[j].jsonWrite(writeJson, currentBean);
          }
        }
        props = desc.propertiesTransient();
        for (int j = 0; j < props.length; j++) {
          if (isIncludeProperty(props[j])) {
            props[j].jsonWrite(writeJson, currentBean);
          }
        }
      }
    }
  }


  public Boolean includeMany(String key) {
    if (pathProperties != null) {
      String fullPath = pathStack.peekFullPath(key);
      return pathProperties.hasPath(fullPath);
    }
    return null;
  }

  public void toJson(String name, Collection<?> c) throws IOException {

    beginAssocMany(name);

    for (Object bean : c) {
      BeanDescriptor<?> d = getDescriptor(bean.getClass());
      d.jsonWrite(this, (EntityBean)bean, null);
    }
    endAssocMany();
  }

  private <T> BeanDescriptor<T> getDescriptor(Class<T> cls) {
    BeanDescriptor<T> d = server.getBeanDescriptor(cls);
    if (d == null) {
      throw new RuntimeException("No BeanDescriptor found for " + cls);
    }
    return d;
  }

  public void beginAssocMany(String key) throws IOException {
    pathStack.pushPathKey(key);
    generator.writeFieldName(key);
    generator.writeStartArray();
  }

  public void endAssocMany() throws IOException {
    pathStack.pop();
    generator.writeEndArray();
  }

  public void writeStartArray(String key) throws IOException {
    if (key != null) {
      generator.writeFieldName(key);
    } 
    generator.writeStartArray();  
  }
  
  public void writeStartObject(String key) throws IOException {
    if (key != null) {
      generator.writeFieldName(key);
    }
    generator.writeStartObject();
  }
  
  public void writeNull(String name) throws IOException {
    generator.writeNullField(name);
  }

  public void writeEndObject() throws IOException {
    generator.writeEndObject();
  }
  
  public void writeEndArray() throws IOException {
    generator.writeEndArray();
  }
}
