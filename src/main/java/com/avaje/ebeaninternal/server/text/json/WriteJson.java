package com.avaje.ebeaninternal.server.text.json;

import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebean.config.JsonConfig;
import com.avaje.ebean.text.PathProperties;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.deploy.BeanProperty;
import com.avaje.ebeaninternal.server.type.JsonWriter;
import com.avaje.ebeaninternal.server.util.ArrayStack;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class WriteJson implements JsonWriter {

  private final SpiEbeanServer server;
  
  private final JsonGenerator generator;
  
  private final PathProperties pathProperties;

  private final PathStack pathStack;

  private final ArrayStack<Object> parentBeans;

  private final Object objectMapper;

  private final JsonConfig.Include include;

  /**
   * Construct for full bean use (normal).
   */
  public WriteJson(SpiEbeanServer server, JsonGenerator generator, PathProperties pathProperties, Object objectMapper, JsonConfig.Include include){
    this.server = server;
    this.generator = generator;
    this.pathProperties = pathProperties;
    this.objectMapper = objectMapper;
    this.include = include;
    this.parentBeans = new ArrayStack<Object>();
    this.pathStack = new PathStack();
  }

  /**
   * Construct for Json scalar use.
   */
  public WriteJson(JsonGenerator generator, JsonConfig.Include include){
    this.generator = generator;
    this.include = include;
    this.server = null;
    this.pathProperties = null;
    this.objectMapper = null;
    this.parentBeans = null;
    this.pathStack = null;
  }

  /**
   * Return true if null values should be included in JSON output.
   */
  public boolean isIncludeNull() {
    return include == JsonConfig.Include.ALL;
  }

  /**
   * Return true if empty collections should be included in the JSON output.
   */
  public boolean isIncludeEmpty() {
    return include != JsonConfig.Include.NON_EMPTY;
  }

  public JsonGenerator gen() {
    return generator;
  }

  @Override
  public void writeFieldName(String name) throws IOException {
    generator.writeFieldName(name);
  }

  @Override
  public void writeNullField(String name) throws IOException {
    if (isIncludeNull()) {
      generator.writeNullField(name);
    }
  }

  @Override
  public void writeNumberField(String name, Long value) throws IOException {
    generator.writeNumberField(name, value);
  }

  @Override
  public void writeNumberField(String name, Double value) throws IOException {
    generator.writeNumberField(name, value);
  }

  @Override
  public void writeNumberField(String name, int value) throws IOException {
    generator.writeNumberField(name, value);
  }

  @Override
  public void writeNumberField(String name, Short value) throws IOException {
    generator.writeNumberField(name, value);
  }


  @Override
  public void writeNumberField(String name, Float value) throws IOException {
    generator.writeNumberField(name, value);
  }


  @Override
  public void writeNumberField(String name, BigDecimal value) throws IOException {
    generator.writeNumberField(name, value);
  }

  @Override
  public void writeStringField(String name, String value) throws IOException {
    generator.writeStringField(name, value);
  }

  @Override
  public void writeBinary(InputStream is, int length) throws IOException {
    generator.writeBinary(is, length);
  }

  @Override
  public void writeBinaryField(String name, byte[] value) throws IOException {
    generator.writeBinaryField(name, value);
  }

  @Override
  public void writeBooleanField(String name, Boolean value) throws IOException {
    generator.writeBooleanField(name, value);
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

  public void writeValueUsingObjectMapper(String name, Object value) throws IOException {

    if (!isIncludeEmpty()) {
      // check for suppression of empty collection or map
      if (value instanceof Collection && ((Collection)value).isEmpty()) {
        // suppress empty collection
        return;
      } else if (value instanceof Map && ((Map)value).isEmpty()) {
        // suppress empty map
        return;
      }
    }
    generator.writeFieldName(name);
    objectMapper().writeValue(generator, value);
  }

  private ObjectMapper objectMapper() {
    if (objectMapper == null) {
      throw new IllegalStateException(
          "Jackson ObjectMapper required but not set. Expected to be set on either"
          +" serverConfig");
    }
    return (ObjectMapper)objectMapper;
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

    private boolean isIncludeTransientProperty(BeanProperty prop) {
      if (!explicitAllProps && currentIncludeProps != null) {
        // explicitly controlled by pathProperties
        return currentIncludeProps.contains(prop.getName());
      } else {
        // by default include transient properties
        return true;
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
          if (isIncludeTransientProperty(props[j])) {
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

  public void writeEndObject() throws IOException {
    generator.writeEndObject();
  }
  
  public void writeEndArray() throws IOException {
    generator.writeEndArray();
  }
}
