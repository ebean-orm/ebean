package com.avaje.ebeaninternal.server.text.json;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import javax.json.stream.JsonGenerator;

import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebean.text.PathProperties;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.deploy.BeanProperty;
import com.avaje.ebeaninternal.server.util.ArrayStack;

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
    if (parentBeans.isEmpty()) {
      return false;
    } else {
      return parentBeans.contains(bean);
    }
  }

  public void pushParentBeanMany(Object parentBean) {
    parentBeans.push(parentBean);
  }

  public void popParentBeanMany() {
    parentBeans.pop();
  }

  public void beginAssocOne(String key, EntityBean bean) {
    parentBeans.push(bean);
    pathStack.pushPathKey(key);
  }

  public void endAssocOne() {
    parentBeans.pop();
    pathStack.pop();
  }

  public Set<String> getIncludeProperties() {

    if (pathProperties == null) {
      return null;
    } else {
      return pathProperties.get(pathStack.peekWithNull());
    }
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

  public class WriteBean {
    
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

    public void write(WriteJson writeJson) {
      //EntityBean bean = writeJson.getBean();
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
          System.out.println("bean "+ currentBean+" prop:"+props[j]);
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

  public void toJson(String name, Collection<?> c) {

    beginAssocMany(name);

    Iterator<?> it = c.iterator();
    while (it.hasNext()) {
      EntityBean o = (EntityBean) it.next();
      BeanDescriptor<?> d = getDecriptor(o.getClass());
      d.jsonWrite(this, o, null);
    }
    endAssocMany();
  }

  private <T> BeanDescriptor<T> getDecriptor(Class<T> cls) {
    BeanDescriptor<T> d = server.getBeanDescriptor(cls);
    if (d == null) {
      String msg = "No BeanDescriptor found for " + cls;
      throw new RuntimeException(msg);
    }
    return d;
  }

  public void beginAssocMany(String key) {
    pathStack.pushPathKey(key);
    generator.writeStartArray(key);
  }

  public void endAssocMany() {
    pathStack.pop();
    generator.writeEnd();
  }

  public void writeStartObject(String key) {
    if (key == null) {
      generator.writeStartObject();
    } else {
      generator.writeStartObject(key);
    }
  }

}
