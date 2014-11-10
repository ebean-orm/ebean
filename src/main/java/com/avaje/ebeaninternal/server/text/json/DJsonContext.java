package com.avaje.ebeaninternal.server.text.json;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebean.json.EJson;
import com.avaje.ebean.text.PathProperties;
import com.avaje.ebean.text.TextException;
import com.avaje.ebean.text.json.JsonContext;
import com.avaje.ebean.text.json.JsonWriteOptions;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.util.ParamTypeHelper;
import com.avaje.ebeaninternal.util.ParamTypeHelper.ManyType;
import com.avaje.ebeaninternal.util.ParamTypeHelper.TypeInfo;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

/**
 * Default implementation of JsonContext.
 * 
 * @author rbygrave
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

    BeanDescriptor<T> d = getDecriptor(cls);
    return d.jsonRead(parser, null);
  }

  public <T> List<T> toList(Class<T> cls, String json) throws IOException {
    return toList(cls, new StringReader(json));
  }


  public <T> List<T> toList(Class<T> cls, Reader jsonReader) throws IOException {
    return toList(cls, createParser(jsonReader));
  }

  private <T> List<T> toList(Class<T> cls, JsonParser src) throws IOException {

    try {
      BeanDescriptor<T> d = getDecriptor(cls);

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

    } catch (RuntimeException e) {
      throw new TextException("Error parsing " + src, e);
    }
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
      throw new TextException("Type " + manyType + " not supported");
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
      throw new TextException("Type " + manyType + " not supported");
    }
  }

  public void toJsonWriter(Object o, Writer writer) throws IOException {
    toJsonWriter(o, writer, null);
  }


  public void toJsonWriter(Object o, Writer writer, JsonWriteOptions options) throws IOException {
    JsonGenerator generator = createGenerator(writer);
    toJsonInternal(o, generator, options);
    generator.close();
  }

  public String toJsonString(Object o) throws IOException {
    return toJsonString(o, null);
  }

  public String toJsonString(Object o, JsonWriteOptions options) throws IOException {
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
      gen.writeBoolean(((Boolean) o).booleanValue());
    } else if (o instanceof String) {
      gen.writeString((String) o);

      // } else if (o instanceof JsonElement) {

    } else if (o instanceof Map<?, ?>) {
      toJsonFromMap((Map<Object, Object>) o, gen, options);

    } else if (o instanceof Collection<?>) {
      toJsonFromCollection((Collection<?>) o, null, gen, options);

    } else if (o instanceof EntityBean) {
      BeanDescriptor<?> d = getDecriptor(o.getClass());
      WriteJson writeJson = createWriteJson(gen, options);
      d.jsonWrite(writeJson, (EntityBean)o, null);
    }
  }

  private WriteJson createWriteJson(JsonGenerator gen, JsonWriteOptions options) {
    PathProperties pathProps = (options == null) ? null : options.getPathProperties();
    return new WriteJson(server, gen, pathProps);
  }

  private <T> void toJsonFromCollection(Collection<T> c, String key, JsonGenerator gen, JsonWriteOptions options) throws IOException {

    if (key != null) {
      gen.writeFieldName(key);      
    }
    gen.writeStartArray();

    WriteJson writeJson = createWriteJson(gen, options);

    Iterator<T> it = c.iterator();
    while (it.hasNext()) {
      T t = it.next();
      BeanDescriptor<?> d = getDecriptor(t.getClass());
      d.jsonWrite(writeJson, (EntityBean)t, null);
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
          BeanDescriptor<?> d = getDecriptor(value.getClass());
          d.jsonWrite(writeJson,(EntityBean) value, key);

        } else {
          EJson.write(entry, gen);
        }
      }
    }
    gen.writeEndObject();
  }

  private <T> BeanDescriptor<T> getDecriptor(Class<T> cls) {
    BeanDescriptor<T> d = server.getBeanDescriptor(cls);
    if (d == null) {
      throw new RuntimeException("No BeanDescriptor found for " + cls);
    }
    return d;
  }
}
