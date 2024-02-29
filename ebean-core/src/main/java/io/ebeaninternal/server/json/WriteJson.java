package io.ebeaninternal.server.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.ebean.FetchPath;
import io.ebean.bean.EntityBean;
import io.ebean.config.JsonConfig;
import io.ebean.text.json.EJson;
import io.ebean.text.json.JsonIOException;
import io.ebean.text.json.JsonWriteBeanVisitor;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.api.json.SpiJsonWriter;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.deploy.BeanProperty;
import io.ebeaninternal.server.util.ArrayStack;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

public final class WriteJson implements SpiJsonWriter {

  private final SpiEbeanServer server;
  private final JsonGenerator generator;
  private final FetchPath fetchPath;
  private final Map<String, JsonWriteBeanVisitor<?>> visitors;
  private final PathStack pathStack;
  private final ArrayStack<Object> parentBeans;
  private final Object objectMapper;
  private final JsonConfig.Include include;
  private final boolean includeLoadedImplicit;


  /**
   * Construct for full bean use (normal).
   */
  public WriteJson(SpiEbeanServer server, JsonGenerator generator, FetchPath fetchPath,
                   Map<String, JsonWriteBeanVisitor<?>> visitors, Object objectMapper, JsonConfig.Include include,
                   boolean includeLoadedImplicit) {

    this.server = server;
    this.generator = generator;
    this.fetchPath = fetchPath;
    this.visitors = visitors;
    this.objectMapper = objectMapper;
    this.include = include;
    this.includeLoadedImplicit = includeLoadedImplicit;
    this.parentBeans = new ArrayStack<>();
    this.pathStack = new PathStack();
  }

  /**
   * Construct for Json scalar use.
   */
  public WriteJson(JsonGenerator generator, JsonConfig.Include include) {
    this.generator = generator;
    this.include = include;
    this.includeLoadedImplicit = true;
    this.visitors = null;
    this.server = null;
    this.fetchPath = null;
    this.objectMapper = null;
    this.parentBeans = null;
    this.pathStack = null;
  }

  /**
   * Return true if null values should be included in JSON output.
   */
  @Override
  public boolean isIncludeNull() {
    return include == JsonConfig.Include.ALL;
  }

  /**
   * Return true if empty collections should be included in the JSON output.
   */
  @Override
  public boolean isIncludeEmpty() {
    return include != JsonConfig.Include.NON_EMPTY;
  }

  @Override
  public JsonGenerator gen() {
    return generator;
  }

  @Override
  public void flush() throws IOException {
    generator.flush();
  }

  @Override
  public void writeStartObject(String key) {
    try {
      if (key != null) {
        generator.writeFieldName(key);
      }
      generator.writeStartObject();
    } catch (IOException e) {
      throw new JsonIOException(e);
    }
  }

  @Override
  public void writeStartObject() {
    try {
      generator.writeStartObject();
    } catch (IOException e) {
      throw new JsonIOException(e);
    }
  }

  @Override
  public void writeEndObject() {
    try {
      generator.writeEndObject();
    } catch (IOException e) {
      throw new JsonIOException(e);
    }
  }

  @Override
  public void writeStartArray(String key) {
    try {
      if (key != null) {
        generator.writeFieldName(key);
      }
      generator.writeStartArray();
    } catch (IOException e) {
      throw new JsonIOException(e);
    }
  }

  @Override
  public void writeStartArray() {
    try {
      generator.writeStartArray();
    } catch (IOException e) {
      throw new JsonIOException(e);
    }
  }

  @Override
  public void writeEndArray() {
    try {
      generator.writeEndArray();
    } catch (IOException e) {
      throw new JsonIOException(e);
    }
  }

  @Override
  public void writeRaw(String text) {
    try {
      generator.writeRaw(text);
    } catch (IOException e) {
      throw new JsonIOException(e);
    }
  }

  @Override
  public void writeRawValue(String text) {
    try {
      generator.writeRawValue(text);
    } catch (IOException e) {
      throw new JsonIOException(e);
    }
  }

  @Override
  public void writeFieldName(String name) {
    try {
      generator.writeFieldName(name);
    } catch (IOException e) {
      throw new JsonIOException(e);
    }
  }

  @Override
  public void writeNullField(String name) {
    if (isIncludeNull()) {
      try {
        generator.writeNullField(name);
      } catch (IOException e) {
        throw new JsonIOException(e);
      }
    }
  }

  @Override
  public void writeNumberField(String name, long value) {
    try {
      generator.writeNumberField(name, value);
    } catch (IOException e) {
      throw new JsonIOException(e);
    }
  }

  @Override
  public void writeNumberField(String name, double value) {
    try {
      generator.writeNumberField(name, value);
    } catch (IOException e) {
      throw new JsonIOException(e);
    }
  }

  @Override
  public void writeNumberField(String name, int value) {
    try {
      generator.writeNumberField(name, value);
    } catch (IOException e) {
      throw new JsonIOException(e);
    }
  }

  @Override
  public void writeNumberField(String name, short value) {
    try {
      generator.writeNumberField(name, value);
    } catch (IOException e) {
      throw new JsonIOException(e);
    }
  }


  @Override
  public void writeNumberField(String name, float value) {
    try {
      generator.writeNumberField(name, value);
    } catch (IOException e) {
      throw new JsonIOException(e);
    }
  }


  @Override
  public void writeNumberField(String name, BigDecimal value) {
    try {
      generator.writeNumberField(name, value);
    } catch (IOException e) {
      throw new JsonIOException(e);
    }
  }

  @Override
  public void writeStringField(String name, String value) {
    try {
      generator.writeStringField(name, value);
    } catch (IOException e) {
      throw new JsonIOException(e);
    }
  }

  @Override
  public void writeBinary(InputStream is, int length) {
    try {
      generator.writeBinary(is, length);
    } catch (IOException e) {
      throw new JsonIOException(e);
    }
  }

  @Override
  public void writeBinaryField(String name, byte[] value) {
    try {
      generator.writeBinaryField(name, value);
    } catch (IOException e) {
      throw new JsonIOException(e);
    }
  }

  @Override
  public void writeBooleanField(String name, boolean value) {
    try {
      generator.writeBooleanField(name, value);
    } catch (IOException e) {
      throw new JsonIOException(e);
    }
  }

  @Override
  public void writeBoolean(boolean value) {
    try {
      generator.writeBoolean(value);
    } catch (IOException e) {
      throw new JsonIOException(e);
    }
  }

  @Override
  public void writeString(String value) {
    try {
      generator.writeString(value);
    } catch (IOException e) {
      throw new JsonIOException(e);
    }
  }

  @Override
  public void writeNumber(int value) {
    try {
      generator.writeNumber(value);
    } catch (IOException e) {
      throw new JsonIOException(e);
    }
  }

  @Override
  public void writeNumber(long value) {
    try {
      generator.writeNumber(value);
    } catch (IOException e) {
      throw new JsonIOException(e);
    }
  }

  @Override
  public void writeNumber(double value) {
    try {
      generator.writeNumber(value);
    } catch (IOException e) {
      throw new JsonIOException(e);
    }
  }

  @Override
  public void writeNumber(BigDecimal value) {
    try {
      generator.writeNumber(value);
    } catch (IOException e) {
      throw new JsonIOException(e);
    }
  }

  @Override
  public void writeNull() {
    try {
      generator.writeNull();
    } catch (IOException e) {
      throw new JsonIOException(e);
    }
  }

  @Override
  public boolean parentBean(Object bean) {
    return !parentBeans.isEmpty() && parentBeans.contains(bean);
  }

  @Override
  public void pushParentBeanMany(EntityBean parentBean) {
    parentBeans.push(parentBean);
  }

  @Override
  public void popParentBeanMany() {
    parentBeans.pop();
  }

  @Override
  public void beginAssocOne(String key, EntityBean bean) {
    parentBeans.push(bean);
    pathStack.pushPathKey(key);
  }

  @Override
  public void endAssocOne() {
    parentBeans.pop();
    pathStack.pop();
  }

  @Override
  public void beginAssocMany(String key) {
    try {
      pathStack.pushPathKey(key);
      if (key != null) {
        generator.writeFieldName(key);
      }
      generator.writeStartArray();
    } catch (IOException e) {
      throw new JsonIOException(e);
    }
  }

  @Override
  public void endAssocMany() {
    try {
      pathStack.pop();
      generator.writeEndArray();
    } catch (IOException e) {
      throw new JsonIOException(e);
    }
  }

  @Override
  public void beginAssocManyMap(String key, boolean elementCollection) {
    try {
      pathStack.pushPathKey(key);
      if (key != null) {
        generator.writeFieldName(key);
      }
      if (elementCollection) {
        generator.writeStartObject();
      } else {
        generator.writeStartArray();
      }
    } catch (IOException e) {
      throw new JsonIOException(e);
    }
  }

  @Override
  public void endAssocManyMap(boolean elementCollection) {
    try {
      pathStack.pop();
      if (elementCollection) {
        generator.writeEndObject();
      } else {
        generator.writeEndArray();
      }
    } catch (IOException e) {
      throw new JsonIOException(e);
    }
  }

  @Override
  public <T> void writeBean(BeanDescriptor<T> desc, EntityBean bean) {
    createWriteBean(desc, bean).write(this);
  }

  private <T> WriteBean createWriteBean(BeanDescriptor<T> desc, EntityBean bean) {

    String path = pathStack.peekWithNull();
    JsonWriteBeanVisitor<?> visitor = (visitors == null) ? null : visitors.get(path);
    if (fetchPath == null) {
      return new WriteBean(desc, bean, visitor);
    }

    boolean explicitAllProps = false;
    Set<String> currentIncludeProps = fetchPath.getProperties(path);
    if (currentIncludeProps != null) {
      explicitAllProps = currentIncludeProps.contains("*");
      if (explicitAllProps || currentIncludeProps.isEmpty()) {
        currentIncludeProps = null;
      }
    }
    return new WriteBean(desc, explicitAllProps, includeLoadedImplicit, currentIncludeProps, bean, visitor);
  }

  @Override
  public void writeValueUsingObjectMapper(String name, Object value) {

    if (!isIncludeEmpty()) {
      // check for suppression of empty collection or map
      if (value instanceof Collection && ((Collection<?>) value).isEmpty()) {
        // suppress empty collection
        return;
      } else if (value instanceof Map && ((Map<?,?>) value).isEmpty()) {
        // suppress empty map
        return;
      }
    }
    try {
      generator.writeFieldName(name);
      objectMapper().writeValue(generator, value);
    } catch (IOException e) {
      throw new JsonIOException(e);
    }
  }

  private ObjectMapper objectMapper() {
    if (objectMapper == null) {
      throw new IllegalStateException("Jackson ObjectMapper required but not set. Expected to be set on DatabaseConfig");
    }
    return (ObjectMapper) objectMapper;
  }

  public static class WriteBean {

    final boolean explicitAllProps;
    final boolean includeLoadedImplicit;
    final Set<String> currentIncludeProps;
    final BeanDescriptor<?> desc;
    final EntityBean currentBean;

    @SuppressWarnings("rawtypes")
    final JsonWriteBeanVisitor visitor;

    WriteBean(BeanDescriptor<?> desc, EntityBean currentBean, JsonWriteBeanVisitor<?> visitor) {
      this(desc, false, true, null, currentBean, visitor);
    }

    WriteBean(BeanDescriptor<?> desc, boolean explicitAllProps, boolean includeLoadedImplicit, Set<String> currentIncludeProps, EntityBean currentBean, JsonWriteBeanVisitor<?> visitor) {
      super();
      this.desc = desc;
      this.currentBean = currentBean;
      this.explicitAllProps = explicitAllProps;
      this.includeLoadedImplicit = includeLoadedImplicit;
      this.currentIncludeProps = currentIncludeProps;
      this.visitor = visitor;
    }

    private boolean isReferenceOnly() {
      return !explicitAllProps && currentIncludeProps == null && currentBean._ebean_getIntercept().isReference();
    }

    private boolean isIncludeProperty(BeanProperty prop) {
      if (explicitAllProps)
        return true;
      if (currentIncludeProps != null) {
        // explicitly controlled by pathProperties
        if (prop.isId() && currentIncludeProps.contains("${identifier}")) {
          return true;
        }
        return currentIncludeProps.contains(prop.name());
      } else if (includeLoadedImplicit){
        // include only loaded properties
        return currentBean._ebean_getIntercept().isLoadedProperty(prop.propertyIndex());
      } else {
        return prop.isId();
      }
    }

    private boolean isIncludeTransientProperty(BeanProperty prop) {
      if (prop.isUnmappedJson()) {
        return false;
      } else if (!explicitAllProps && currentIncludeProps != null) {
        // explicitly controlled by pathProperties
        return currentIncludeProps.contains(prop.name());
      } else {
        // by default include transient properties
        return true;
      }
    }

    @SuppressWarnings("unchecked")
    public void write(WriteJson writeJson) {

      try {
        BeanProperty beanProp = desc.idProperty();
        if (beanProp != null) {
          if (isIncludeProperty(beanProp)) {
            beanProp.jsonWrite(writeJson, currentBean);
          }
        }

        if (!isReferenceOnly()) {
          // render all the properties and invoke lazy loading if required
          BeanProperty[] props = desc.propertiesNonTransient();
          for (BeanProperty prop1 : props) {
            if (isIncludeProperty(prop1)) {
              prop1.jsonWrite(writeJson, currentBean);
            }
          }
          props = desc.propertiesTransient();
          for (BeanProperty prop : props) {
            if (isIncludeTransientProperty(prop)) {
              prop.jsonWrite(writeJson, currentBean);
            }
          }
        }

        BeanProperty unmappedJson = desc.propertyUnmappedJson();
        if (unmappedJson != null && unmappedJson.isJsonSerialize()) {
          Map<String,Object> map = (Map<String,Object>)unmappedJson.getValue(currentBean);
          if (map != null) {
            // write to JSON at the current level
            for (Map.Entry<String, Object> entry : map.entrySet()) {
              writeJson.writeFieldName(entry.getKey());
              EJson.write(entry.getValue(), writeJson.generator);
            }
          }
        }

        if (visitor != null) {
          visitor.visit(currentBean, writeJson);
        }

      } catch (IOException e) {
        throw new JsonIOException(e);
      }
    }
  }

  @Override
  public Boolean includeMany(String key) {
    if (fetchPath != null) {
      String fullPath = pathStack.peekFullPath(key);
      return fetchPath.hasPath(fullPath);
    }
    return null;
  }

  @Override
  public void toJson(String name, Collection<?> c) {

    try {
      beginAssocMany(name);

      for (Object bean : c) {
        BeanDescriptor<?> d = getDescriptor(bean.getClass());
        d.jsonWrite(this, (EntityBean) bean, null);
      }
      endAssocMany();
    } catch (IOException e) {
      throw new JsonIOException(e);
    }
  }

  private <T> BeanDescriptor<T> getDescriptor(Class<T> cls) {
    BeanDescriptor<T> d = server.descriptor(cls);
    if (d == null) {
      throw new RuntimeException("No BeanDescriptor found for " + cls);
    }
    return d;
  }


}
