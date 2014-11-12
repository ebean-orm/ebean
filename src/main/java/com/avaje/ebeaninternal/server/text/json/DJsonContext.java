package com.avaje.ebeaninternal.server.text.json;

import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebean.json.EJson;
import com.avaje.ebean.text.PathProperties;
import com.avaje.ebean.text.json.JsonContext;
import com.avaje.ebean.text.json.JsonWriteOptions;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.util.ParamTypeHelper;
import com.avaje.ebeaninternal.util.ParamTypeHelper.ManyType;
import com.avaje.ebeaninternal.util.ParamTypeHelper.TypeInfo;
import com.fasterxml.jackson.core.*;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;
import java.util.Map.Entry;

/**
 * Default implementation of JsonContext.
 */
public class DJsonContext implements JsonContext {

  private final SpiEbeanServer server;
  
  private final JsonFactory jsonFactory;

  public DJsonContext(SpiEbeanServer server, JsonFactory jsonFactory) {
    this.server = server;
    this.jsonFactory = (jsonFactory != null) ? jsonFactory : new JsonFactory();
  }

  public boolean isSupportedType(Type genericType) {
    return server.isSupportedType(genericType);
  }

  public JsonGenerator createGenerator(Writer writer) throws IOException {
    return jsonFactory.createGenerator(writer);
  }
  
  public JsonParser createParser(Reader reader) throws IOException {
    return jsonFactory.createParser(reader);
  }

  public <T> T toBean(Class<T> cls, String json) throws IOException {
    return toBean(cls, new StringReader(json));
  }

  public <T> T toBean(Class<T> cls, Reader jsonReader) throws IOException {
    return toBean(cls, createParser(jsonReader));
  }

  private <T> T toBean(Class<T> cls, JsonParser parser) throws IOException {

    BeanDescriptor<T> d = getDescriptor(cls);
    return d.jsonRead(parser, null);
  }

  public <T> List<T> toList(Class<T> cls, String json) throws IOException {
    return toList(cls, new StringReader(json));
  }


  public <T> List<T> toList(Class<T> cls, Reader jsonReader) throws IOException {
    return toList(cls, createParser(jsonReader));
  }

  private <T> List<T> toList(Class<T> cls, JsonParser src) throws IOException {

    BeanDescriptor<T> d = getDescriptor(cls);

    List<T> list = new ArrayList<T>();

    JsonToken event = src.nextToken();
    if (event != JsonToken.START_ARRAY) {
      throw new JsonParseException("Expecting start_array event but got " + event ,src.getCurrentLocation());
    }

    do {
      T bean = d.jsonRead(src, null);
      if (bean == null) {
        break;
      } else {
        list.add(bean);
      }
    } while (true);

    return list;
  }

  public Object toObject(Type genericType, String json) throws IOException {

    TypeInfo info = ParamTypeHelper.getTypeInfo(genericType);
    ManyType manyType = info.getManyType();
    switch (manyType) {
    case NONE:
      return toBean(info.getBeanType(), json);

    case LIST:
      return toList(info.getBeanType(), json);

    default:
      throw new IOException("Type " + manyType + " not supported");
    }
  }

  public Object toObject(Type genericType, Reader json) throws IOException {

    TypeInfo info = ParamTypeHelper.getTypeInfo(genericType);
    ManyType manyType = info.getManyType();
    switch (manyType) {
    case NONE:
      return toBean(info.getBeanType(), json);

    case LIST:
      return toList(info.getBeanType(), json);

    default:
      throw new IOException("Type " + manyType + " not supported");
    }
  }

  public void toJson(Object o, Writer writer) throws IOException {
    toJson(o, writer, null);
  }


  public void toJson(Object o, Writer writer, JsonWriteOptions options) throws IOException {
    JsonGenerator generator = createGenerator(writer);
    toJsonInternal(o, generator, options);
    generator.close();
  }

  public String toJson(Object o) throws IOException {
    return toJsonString(o, null);
  }

  public String toJson(Object o, JsonWriteOptions options) throws IOException {
    return toJsonString(o, options);
  }

  private String toJsonString(Object o, JsonWriteOptions options) throws IOException {
    StringWriter writer = new StringWriter(500);
    JsonGenerator gen = createGenerator(writer);
    toJsonInternal(o, gen, options);
    gen.close();
    return writer.toString();
  }

  @SuppressWarnings("unchecked")
  private void toJsonInternal(Object o, JsonGenerator gen, JsonWriteOptions options) throws IOException {

    if (o == null) {
      gen.writeNull();
    } else if (o instanceof Number) {
      gen.writeNumber(((Number) o).doubleValue());
    } else if (o instanceof Boolean) {
      gen.writeBoolean((Boolean) o);
    } else if (o instanceof String) {
      gen.writeString((String) o);

      // } else if (o instanceof JsonElement) {

    } else if (o instanceof Map<?, ?>) {
      toJsonFromMap((Map<Object, Object>) o, gen, options);

    } else if (o instanceof Collection<?>) {
      toJsonFromCollection((Collection<?>) o, null, gen, options);

    } else if (o instanceof EntityBean) {
      BeanDescriptor<?> d = getDescriptor(o.getClass());
      WriteJson writeJson = createWriteJson(gen, options);
      d.jsonWrite(writeJson, (EntityBean)o, null);
    }
  }

  private WriteJson createWriteJson(JsonGenerator gen, JsonWriteOptions options) {
    PathProperties pathProps = (options == null) ? null : options.getPathProperties();
    return new WriteJson(server, gen, pathProps);
  }

  private <T> void toJsonFromCollection(Collection<T> collection, String key, JsonGenerator gen, JsonWriteOptions options) throws IOException {

    if (key != null) {
      gen.writeFieldName(key);      
    }
    gen.writeStartArray();

    WriteJson writeJson = createWriteJson(gen, options);

    for (T bean : collection) {
      BeanDescriptor<?> d = getDescriptor(bean.getClass());
      d.jsonWrite(writeJson, (EntityBean) bean, null);
    }
    gen.writeEndArray();
  }

  private void toJsonFromMap(Map<Object, Object> map, JsonGenerator gen, JsonWriteOptions options) throws IOException {

    Set<Entry<Object, Object>> entrySet = map.entrySet();
    Iterator<Entry<Object, Object>> it = entrySet.iterator();

    WriteJson writeJson = createWriteJson(gen, options);
    gen.writeStartObject();
    
    while (it.hasNext()) {
      Entry<Object, Object> entry = it.next();
      String key = entry.getKey().toString();
      Object value = entry.getValue();
      if (value == null) {
        gen.writeNullField(key);
      } else {
        if (value instanceof Collection<?>) {
          toJsonFromCollection((Collection<?>) value, key, gen, options);

        } else if (value instanceof EntityBean) {
          BeanDescriptor<?> d = getDescriptor(value.getClass());
          d.jsonWrite(writeJson,(EntityBean) value, key);

        } else {
          EJson.write(entry, gen);
        }
      }
    }
    gen.writeEndObject();
  }

  private <T> BeanDescriptor<T> getDescriptor(Class<T> cls) {
    BeanDescriptor<T> d = server.getBeanDescriptor(cls);
    if (d == null) {
      throw new RuntimeException("No BeanDescriptor found for " + cls);
    }
    return d;
  }
}
