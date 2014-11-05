package com.avaje.ebeaninternal.server.text.json;

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

import javax.json.Json;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebean.text.PathProperties;
import com.avaje.ebean.text.TextException;
import com.avaje.ebean.text.json.JsonContext;
import com.avaje.ebean.text.json.JsonElement;
import com.avaje.ebean.text.json.JsonReadOptions;
import com.avaje.ebean.text.json.JsonValueAdapter;
import com.avaje.ebean.text.json.JsonWriteOptions;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.util.ParamTypeHelper;
import com.avaje.ebeaninternal.util.ParamTypeHelper.ManyType;
import com.avaje.ebeaninternal.util.ParamTypeHelper.TypeInfo;

/**
 * Default implementation of JsonContext.
 * 
 * @author rbygrave
 */
public class DJsonContext implements JsonContext {

  private final SpiEbeanServer server;

  //private final JsonValueAdapter dfltValueAdapter;

  private final boolean dfltPretty;

  public DJsonContext(SpiEbeanServer server, JsonValueAdapter dfltValueAdapter, boolean dfltPretty) {
    this.server = server;
    //this.dfltValueAdapter = dfltValueAdapter;
    this.dfltPretty = dfltPretty;
  }

  public boolean isSupportedType(Type genericType) {
    return server.isSupportedType(genericType);
  }

  private JsonParser createReader(Reader jsonReader) {
    return Json.createParser(jsonReader);
  }

  public <T> T toBean(Class<T> cls, String json) {
    return toBean(cls, new StringReader(json), null);
  }

  public <T> T toBean(Class<T> cls, Reader jsonReader) {
    return toBean(cls, createReader(jsonReader), null);
  }

  public <T> T toBean(Class<T> cls, String json, JsonReadOptions options) {
    return toBean(cls, new StringReader(json), options);
  }

  public <T> T toBean(Class<T> cls, Reader jsonReader, JsonReadOptions options) {
    return toBean(cls, createReader(jsonReader), options);
  }

  private <T> T toBean(Class<T> cls, JsonParser parser, JsonReadOptions options) {

    BeanDescriptor<T> d = getDecriptor(cls);
    return d.jsonRead(parser, null);
  }

  public <T> List<T> toList(Class<T> cls, String json) {
    return toList(cls, new StringReader(json), null);
  }

  public <T> List<T> toList(Class<T> cls, String json, JsonReadOptions options) {
    return toList(cls, new StringReader(json), options);
  }

  public <T> List<T> toList(Class<T> cls, Reader jsonReader) {
    return toList(cls, createReader(jsonReader), null);
  }

  public <T> List<T> toList(Class<T> cls, Reader jsonReader, JsonReadOptions options) {
    return toList(cls, createReader(jsonReader), options);
  }

  private <T> List<T> toList(Class<T> cls, JsonParser src, JsonReadOptions options) {

    try {
      BeanDescriptor<T> d = getDecriptor(cls);

      List<T> list = new ArrayList<T>();

      if (!src.hasNext()) {
        return list;
      }
      Event event = src.next();
      if (event != Event.START_ARRAY) {
        throw new TextException("Expecting start_array event but got [" + event + "] at [" + src.getLocation() + "]");
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

  public Object toObject(Type genericType, String json, JsonReadOptions options) {

    TypeInfo info = ParamTypeHelper.getTypeInfo(genericType);
    Class<?> beanType = info.getBeanType();
    if (JsonElement.class.isAssignableFrom(beanType)) {
      return InternalJsonParser.parse(json);
    }

    ManyType manyType = info.getManyType();
    switch (manyType) {
    case NONE:
      return toBean(info.getBeanType(), json, options);

    case LIST:
      return toList(info.getBeanType(), json, options);

    default:
      String msg = "ManyType " + manyType + " not supported yet";
      throw new TextException(msg);
    }
  }

  public Object toObject(Type genericType, Reader json, JsonReadOptions options) {

    TypeInfo info = ParamTypeHelper.getTypeInfo(genericType);
    Class<?> beanType = info.getBeanType();
    if (JsonElement.class.isAssignableFrom(beanType)) {
      return InternalJsonParser.parse(json);
    }

    ManyType manyType = info.getManyType();
    switch (manyType) {
    case NONE:
      return toBean(info.getBeanType(), json, options);

    case LIST:
      return toList(info.getBeanType(), json, options);

    default:
      throw new TextException("ManyType " + manyType + " not supported");
    }
  }

  public void toJsonWriter(Object o, Writer writer) {
    toJsonWriter(o, writer, dfltPretty, null, null);
  }

  public void toJsonWriter(Object o, Writer writer, boolean pretty) {
    toJsonWriter(o, writer, pretty, null, null);
  }

  public void toJsonWriter(Object o, Writer writer, boolean pretty, JsonWriteOptions options) {
    toJsonWriter(o, writer, pretty, null, null);
  }

  public void toJsonWriter(Object o, Writer writer, boolean pretty, JsonWriteOptions options, String callback) {
    JsonGenerator generator = Json.createGenerator(writer);
    toJsonInternal(o, generator, pretty, options, callback);
    generator.close();
  }

  public String toJsonString(Object o) {
    return toJsonString(o, dfltPretty, null);
  }

  public String toJsonString(Object o, boolean pretty) {
    return toJsonString(o, pretty, null);
  }

  public String toJsonString(Object o, boolean pretty, JsonWriteOptions options) {
    return toJsonString(o, pretty, options, null);
  }

  public String toJsonString(Object o, boolean pretty, JsonWriteOptions options, String callback) {
    StringWriter writer = new StringWriter(500);
    JsonGenerator gen = Json.createGenerator(writer);
    toJsonInternal(o, gen, pretty, options, callback);
    gen.close();
    return writer.toString();
  }

  @SuppressWarnings("unchecked")
  private void toJsonInternal(Object o, JsonGenerator gen, boolean pretty, JsonWriteOptions options, String requestCallback) {

    if (o == null) {
      gen.writeNull();
    } else if (o instanceof Number) {
      gen.write(((Number) o).doubleValue());
    } else if (o instanceof Boolean) {
      gen.write(((Boolean) o).booleanValue());
    } else if (o instanceof String) {
      gen.write((String) o);

      // } else if (o instanceof JsonElement) {

    } else if (o instanceof Map<?, ?>) {
      toJsonFromMap((Map<Object, Object>) o, gen, pretty, options, requestCallback);

    } else if (o instanceof Collection<?>) {
      toJsonFromCollection((Collection<?>) o, null, gen, pretty, options, requestCallback);

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

  private <T> void toJsonFromCollection(Collection<T> c, String key, JsonGenerator gen, boolean pretty, JsonWriteOptions options, String requestCallback) {

    if (key == null) {
      gen.writeStartArray();
    } else {
      gen.writeStartArray(key);      
    }

    WriteJson writeJson = createWriteJson(gen, options);

    Iterator<T> it = c.iterator();
    while (it.hasNext()) {
      T t = it.next();
      BeanDescriptor<?> d = getDecriptor(t.getClass());
      //writeJson.setBean();
      d.jsonWrite(writeJson, (EntityBean)t, null);
    }
    gen.writeEnd();
  }

  private void toJsonFromMap(Map<Object, Object> map, JsonGenerator gen, boolean pretty, JsonWriteOptions options, String requestCallback) {

    Set<Entry<Object, Object>> entrySet = map.entrySet();
    Iterator<Entry<Object, Object>> it = entrySet.iterator();

    WriteJson writeJson = createWriteJson(gen, options);
    gen.writeStartObject();
    
    while (it.hasNext()) {
      Entry<Object, Object> entry = it.next();
      String key = entry.getKey().toString();
      Object value = entry.getValue();
      if (value == null) {
        gen.writeNull(key);
      } else {
        if (value instanceof Collection<?>) {
          toJsonFromCollection((Collection<?>) value, key, gen, pretty, options, requestCallback);

        } else if (value instanceof EntityBean) {
          BeanDescriptor<?> d = getDecriptor(value.getClass());
          d.jsonWrite(writeJson,(EntityBean) value, key);

        } else {
          throw new RuntimeException("TODO process primitive");
        }
      }
    }
    gen.writeEnd();
  }

  private <T> BeanDescriptor<T> getDecriptor(Class<T> cls) {
    BeanDescriptor<T> d = server.getBeanDescriptor(cls);
    if (d == null) {
      throw new RuntimeException("No BeanDescriptor found for " + cls);
    }
    return d;
  }
}
