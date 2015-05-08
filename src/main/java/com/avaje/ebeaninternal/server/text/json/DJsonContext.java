package com.avaje.ebeaninternal.server.text.json;

import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebean.text.json.EJson;
import com.avaje.ebean.text.PathProperties;
import com.avaje.ebean.text.json.JsonContext;
import com.avaje.ebean.text.json.JsonIOException;
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

  public JsonGenerator createGenerator(Writer writer) throws JsonIOException {
    try {
      return jsonFactory.createGenerator(writer);
    } catch (IOException e) {
      throw new JsonIOException(e);
    }
  }

  public JsonParser createParser(Reader reader) throws JsonIOException {
    try {
      return jsonFactory.createParser(reader);
    } catch (IOException e) {
      throw new JsonIOException(e);
    }
  }

  public <T> T toBean(Class<T> cls, String json) throws JsonIOException {
    return toBean(cls, new StringReader(json));
  }

  public <T> T toBean(Class<T> cls, Reader jsonReader) throws JsonIOException {
    return toBean(cls, createParser(jsonReader));
  }

  public <T> T toBean(Class<T> cls, JsonParser parser) throws JsonIOException {

    try {
      BeanDescriptor<T> d = getDescriptor(cls);
      return d.jsonRead(parser, null);
    } catch (IOException e) {
      throw new JsonIOException(e);
    }
  }

  public <T> List<T> toList(Class<T> cls, String json) throws JsonIOException {
    return toList(cls, new StringReader(json));
  }


  public <T> List<T> toList(Class<T> cls, Reader jsonReader) throws JsonIOException {
    return toList(cls, createParser(jsonReader));
  }

  public <T> List<T> toList(Class<T> cls, JsonParser src) throws JsonIOException {

    try {
      BeanDescriptor<T> d = getDescriptor(cls);

      List<T> list = new ArrayList<T>();

      JsonToken event = src.nextToken();
      if (event != JsonToken.START_ARRAY) {
        throw new JsonParseException("Expecting start_array event but got " + event, src.getCurrentLocation());
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
    } catch (IOException e) {
      throw new JsonIOException(e);
    }
  }

  public Object toObject(Type genericType, String json) throws JsonIOException {

    return toObject(genericType, createParser(new StringReader(json)));
  }

  public Object toObject(Type genericType, Reader json) throws JsonIOException {

    return toObject(genericType, createParser(json));
  }

  public Object toObject(Type genericType, JsonParser jsonParser) throws JsonIOException {

    TypeInfo info = ParamTypeHelper.getTypeInfo(genericType);
    ManyType manyType = info.getManyType();
    switch (manyType) {
      case NONE:
        return toBean(info.getBeanType(), jsonParser);

      case LIST:
        return toList(info.getBeanType(), jsonParser);

      default:
        throw new JsonIOException("Type " + manyType + " not supported");
    }
  }

  @Override
  public void toJson(Object value, JsonGenerator generator) throws JsonIOException {
    // generator passed in so don't close it
    toJsonNoClose(value, generator, null);
  }

  @Override
  public void toJson(Object value, JsonGenerator generator, PathProperties pathProperties) throws JsonIOException {
    // generator passed in so don't close it
    toJsonNoClose(value, generator, JsonWriteOptions.pathProperties(pathProperties));
  }

  @Override
  public void toJson(Object o, JsonGenerator generator, JsonWriteOptions options) throws JsonIOException {
    // generator passed in so don't close it
    toJsonNoClose(o, generator, options);
  }

  @Override
  public void toJson(Object o, Writer writer) throws JsonIOException {
    // close generator
    toJsonWithClose(o, createGenerator(writer), null);
  }

  @Override
  public String toJson(Object value, PathProperties pathProperties) throws JsonIOException {
    return toJson(value, JsonWriteOptions.pathProperties(pathProperties));
  }

  @Override
  public void toJson(Object o, Writer writer, PathProperties pathProperties) throws JsonIOException {
    // close generator
    toJsonWithClose(o, createGenerator(writer), JsonWriteOptions.pathProperties(pathProperties));
  }

  @Override
  public void toJson(Object o, Writer writer, JsonWriteOptions options) throws JsonIOException {
    // close generator
    toJsonWithClose(o, createGenerator(writer), options);
  }

  /**
   * Write to the JsonGenerator and close when complete.
   */
  private void toJsonWithClose(Object o, JsonGenerator generator, JsonWriteOptions options) throws JsonIOException {
    try {
      toJsonInternal(o, generator, options);
      generator.close();
    } catch (IOException e) {
      throw new JsonIOException(e);
    }
  }

  /**
   * Write to the JsonGenerator and without closing it (as it was created externally).
   */
  private void toJsonNoClose(Object o, JsonGenerator generator, JsonWriteOptions options) throws JsonIOException {
    try {
      toJsonInternal(o, generator, options);
    } catch (IOException e) {
      throw new JsonIOException(e);
    }
  }

  @Override
  public String toJson(Object o) throws JsonIOException {
    return toJsonString(o, null);
  }

  @Override
  public String toJson(Object o, JsonWriteOptions options) throws JsonIOException {
    return toJsonString(o, options);
  }

  private String toJsonString(Object value, JsonWriteOptions options) throws JsonIOException {
    try {
      StringWriter writer = new StringWriter(500);
      JsonGenerator gen = createGenerator(writer);
      toJsonInternal(value, gen, options);
      gen.close();
      return writer.toString();
    } catch (IOException e) {
      throw new JsonIOException(e);
    }
  }

  @SuppressWarnings("unchecked")
  private void toJsonInternal(Object value, JsonGenerator gen, JsonWriteOptions options) throws IOException {

    if (value == null) {
      gen.writeNull();
    } else if (value instanceof Number) {
      gen.writeNumber(((Number) value).doubleValue());
    } else if (value instanceof Boolean) {
      gen.writeBoolean((Boolean) value);
    } else if (value instanceof String) {
      gen.writeString((String) value);

      // } else if (o instanceof JsonElement) {

    } else if (value instanceof Map<?, ?>) {
      toJsonFromMap((Map<Object, Object>) value, gen, options);

    } else if (value instanceof Collection<?>) {
      toJsonFromCollection((Collection<?>) value, null, gen, options);

    } else if (value instanceof EntityBean) {
      BeanDescriptor<?> d = getDescriptor(value.getClass());
      WriteJson writeJson = createWriteJson(gen, options);
      d.jsonWrite(writeJson, (EntityBean) value, null);
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
          d.jsonWrite(writeJson, (EntityBean) value, key);

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
