package io.ebeaninternal.server.json;

import io.avaje.json.JsonReader;
import io.avaje.json.JsonReader.Token;
import io.avaje.json.JsonWriter;
import io.avaje.json.stream.JsonStream;
import io.ebean.FetchPath;
import io.ebean.bean.EntityBean;
import io.ebean.config.JsonConfig;
import io.ebean.plugin.BeanType;
import io.ebean.text.json.EJson;
import io.ebean.text.json.JsonIOException;
import io.ebean.text.json.JsonReadOptions;
import io.ebean.text.json.JsonWriteBeanVisitor;
import io.ebean.text.json.JsonWriteOptions;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.api.SpiJsonContext;
import io.ebeaninternal.api.json.SpiJsonReader;
import io.ebeaninternal.api.json.SpiJsonWriter;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.type.TypeManager;
import io.ebeaninternal.util.ParamTypeHelper;
import io.ebeaninternal.util.ParamTypeHelper.ManyType;
import io.ebeaninternal.util.ParamTypeHelper.TypeInfo;

import java.io.IOException;
import java.io.Reader;
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

/**
 * Default implementation of JsonContext.
 */
public final class DJsonContext implements SpiJsonContext {

  private final SpiEbeanServer server;
  private final JsonStream jsonStream;
  private final Object defaultObjectMapper;
  private final JsonConfig.Include defaultInclude;
  private final DJsonScalar jsonScalar;

  public DJsonContext(SpiEbeanServer server, JsonStream jsonStream, TypeManager typeManager) {
    this.server = server;
    this.jsonStream = jsonStream;
    this.defaultObjectMapper = this.server.config().getObjectMapper();
    this.defaultInclude = this.server.config().getJsonInclude();
    this.jsonScalar = new DJsonScalar(typeManager);
  }

  @Override
  public void writeScalar(JsonWriter generator, Object scalarValue) throws IOException {
    jsonScalar.write(generator, scalarValue);
  }

  @Override
  public boolean isSupportedType(Type genericType) {
    return server.isSupportedType(genericType);
  }

  @Override
  public JsonWriter createGenerator(Writer writer) throws JsonIOException {
    JsonWriter jsonWriter = stream().writer(writer);
    jsonWriter.serializeNulls(defaultInclude == JsonConfig.Include.ALL);
    jsonWriter.serializeEmpty(defaultInclude != JsonConfig.Include.NON_EMPTY);
    return jsonWriter;
  }

  @Override
  public JsonReader createParser(Reader reader) throws JsonIOException {
    try {
      return createParser(readAll(reader));
    } catch (IOException e) {
      throw new JsonIOException(e);
    }
  }

  private JsonStream stream() {
    return (jsonStream != null) ? jsonStream : JsonStream.builder().build();
  }

  @Override
  public <T> T toBean(Class<T> cls, String json) throws JsonIOException {
    return toBean(cls, createParser(json));
  }

  @Override
  public <T> T toBean(Class<T> cls, String json, JsonReadOptions options) throws JsonIOException {
    return toBean(cls, createParser(json), options);
  }

  @Override
  public <T> T toBean(Class<T> cls, Reader jsonReader) throws JsonIOException {
    return toBean(cls, createParser(jsonReader));
  }

  @Override
  public <T> T toBean(Class<T> cls, Reader jsonReader, JsonReadOptions options) throws JsonIOException {
    return toBean(cls, createParser(jsonReader), options);
  }

  @Override
  public <T> T toBean(Class<T> cls, JsonReader parser) throws JsonIOException {
    return toBean(cls, parser, null);
  }

  @Override
  public <T> T toBean(Class<T> cls, JsonReader parser, JsonReadOptions options) throws JsonIOException {
    BeanDescriptor<T> desc = getDescriptor(cls);
    try {
      return desc.jsonRead(new ReadJson(desc, parser, options, determineObjectMapper(options), false, stream()), null, null);
    } catch (IOException e) {
      throw new JsonIOException(e);
    }
  }

  @Override
  public <T> void toBean(T target, String json) throws JsonIOException {
    toBean(target, createParser(json));
  }

  @Override
  public <T> void toBean(T target, String json, JsonReadOptions options) throws JsonIOException {
    toBean(target, createParser(json), options);
  }

  @Override
  public <T> void toBean(T target, Reader jsonReader) throws JsonIOException {
    toBean(target, createParser(jsonReader));
  }

  @Override
  public <T> void toBean(T target, Reader jsonReader, JsonReadOptions options) throws JsonIOException {
    toBean(target, createParser(jsonReader), options);
  }

  @Override
  public <T> void toBean(T target, JsonReader parser) throws JsonIOException {
    toBean(target, parser, null);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> void toBean(T target, JsonReader parser, JsonReadOptions options) throws JsonIOException {
    BeanDescriptor<T> desc = (BeanDescriptor<T>) getDescriptor(target.getClass());
    try {
      desc.jsonRead(new ReadJson(desc, parser, options, determineObjectMapper(options), target != null, stream()), null, target);
    } catch (IOException e) {
      throw new JsonIOException(e);
    }
  }

  @Override
  public <T> DJsonBeanReader<T> createBeanReader(Class<T> cls, JsonReader parser, JsonReadOptions options) throws JsonIOException {
    BeanDescriptor<T> desc = getDescriptor(cls);
    return new DJsonBeanReader<>(desc, new ReadJson(desc, parser, options, determineObjectMapper(options), false, stream()));
  }

  @Override
  public <T> DJsonBeanReader<T> createBeanReader(BeanType<T> beanType, JsonReader parser, JsonReadOptions options) throws JsonIOException {
    BeanDescriptor<T> desc = (BeanDescriptor<T>) beanType;
    SpiJsonReader readJson = new ReadJson(desc, parser, options, determineObjectMapper(options), false, stream());
    return new DJsonBeanReader<>(desc, readJson);
  }

  @Override
  public <T> List<T> toList(Class<T> cls, String json) throws JsonIOException {
    return toList(cls, createParser(json));
  }

  @Override
  public <T> List<T> toList(Class<T> cls, String json, JsonReadOptions options) throws JsonIOException {
    return toList(cls, createParser(json), options);
  }

  @Override
  public <T> List<T> toList(Class<T> cls, Reader jsonReader) throws JsonIOException {
    return toList(cls, createParser(jsonReader));
  }

  @Override
  public <T> List<T> toList(Class<T> cls, Reader jsonReader, JsonReadOptions options) throws JsonIOException {
    return toList(cls, createParser(jsonReader), options);
  }

  @Override
  public <T> List<T> toList(Class<T> cls, JsonReader src) throws JsonIOException {
    return toList(cls, src, null);
  }

  @Override
  public <T> List<T> toList(Class<T> cls, JsonReader src, JsonReadOptions options) throws JsonIOException {
    BeanDescriptor<T> desc = getDescriptor(cls);
    SpiJsonReader readJson = new ReadJson(desc, src, options, determineObjectMapper(options), false, stream());
    try {
      if (src.isNullValue()) {
        return null;
      }
      Token currentToken = src.currentToken();
      if (currentToken != Token.BEGIN_ARRAY) {
        throw new JsonIOException("Expecting BEGIN_ARRAY but got " + currentToken);
      }
      List<T> list = new ArrayList<>();
      src.beginArray();
      while (src.hasNextElement()) {
        T bean = desc.jsonRead(readJson, null, null);
        if (bean != null) {
          list.add(bean);
        }
      }
      return list;
    } catch (IOException e) {
      throw new JsonIOException(e);
    }
  }

  @Override
  public Object toObject(Type genericType, String json) throws JsonIOException {
    return toObject(genericType, createParser(json));
  }

  @Override
  public Object toObject(Type genericType, Reader json) throws JsonIOException {
    return toObject(genericType, createParser(json));
  }

  private JsonReader createParser(String json) {
    return stream().reader(json);
  }

  private String readAll(Reader reader) throws IOException {
    StringBuilder builder = new StringBuilder();
    char[] buffer = new char[2048];
    int len;
    while ((len = reader.read(buffer)) != -1) {
      builder.append(buffer, 0, len);
    }
    return builder.toString();
  }

  @Override
  public Object toObject(Type genericType, JsonReader jsonParser) throws JsonIOException {
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
  public void toJson(Object value, JsonWriter generator) throws JsonIOException {
    // generator passed in so don't close it
    toJsonNoClose(value, generator, null);
  }

  @Override
  public void toJson(Object value, JsonWriter generator, FetchPath fetchPath) throws JsonIOException {
    // generator passed in so don't close it
    toJsonNoClose(value, generator, JsonWriteOptions.pathProperties(fetchPath));
  }

  @Override
  public void toJson(Object o, JsonWriter generator, JsonWriteOptions options) throws JsonIOException {
    // generator passed in so don't close it
    toJsonNoClose(o, generator, options);
  }

  @Override
  public void toJson(Object o, Writer writer) throws JsonIOException {
    // close generator
    toJsonWithClose(o, createGenerator(writer), null);
  }

  @Override
  public String toJson(Object value, FetchPath fetchPath) throws JsonIOException {
    return toJson(value, JsonWriteOptions.pathProperties(fetchPath));
  }

  @Override
  public void toJson(Object o, Writer writer, FetchPath fetchPath) throws JsonIOException {
    // close generator
    toJsonWithClose(o, createGenerator(writer), JsonWriteOptions.pathProperties(fetchPath));
  }

  @Override
  public void toJson(Object o, Writer writer, JsonWriteOptions options) throws JsonIOException {
    // close generator
    toJsonWithClose(o, createGenerator(writer), options);
  }

  /**
   * Write to the JsonWriter and close when complete.
   */
  private void toJsonWithClose(Object o, JsonWriter generator, JsonWriteOptions options) throws JsonIOException {
    try {
      toJsonInternal(o, generator, options);
      generator.close();
    } catch (IOException e) {
      throw new JsonIOException(e);
    }
  }

  /**
   * Write to the JsonWriter and without closing it (as it was created externally).
   */
  private void toJsonNoClose(Object o, JsonWriter generator, JsonWriteOptions options) throws JsonIOException {
    try {
      toJsonInternal(o, generator, options);
    } catch (IOException e) {
      throw new JsonIOException(e);
    }
  }

  @Override
  public String toJsonPretty(Object value) throws JsonIOException {
    return toJsonString(value, null, true);
  }

  @Override
  public String toJson(Object o) throws JsonIOException {
    return toJsonString(o, null, false);
  }

  @Override
  public String toJson(Object o, JsonWriteOptions options) throws JsonIOException {
    return toJsonString(o, options, false);
  }

  private String toJsonString(Object value, JsonWriteOptions options, boolean pretty) throws JsonIOException {
    StringWriter writer = new StringWriter(500);
    try (JsonWriter gen = createGenerator(writer)) {
      if (pretty) {
        gen.pretty(true);
      }
      toJsonInternal(value, gen, options);
    } catch (IOException e) {
      throw new JsonIOException(e);
    }
    return writer.toString();
  }

  @SuppressWarnings("unchecked")
  private void toJsonInternal(Object value, JsonWriter gen, JsonWriteOptions options) throws IOException {
    if (value == null) {
      gen.nullValue();
    } else if (value instanceof Number) {
      gen.jsonValue(value);
    } else if (value instanceof Boolean) {
      gen.value((Boolean) value);
    } else if (value instanceof String) {
      gen.value((String) value);
    } else if (value instanceof Map<?, ?>) {
      toJsonFromMap((Map<Object, Object>) value, gen, options);
    } else if (value instanceof Collection<?>) {
      toJsonFromCollection((Collection<?>) value, null, gen, options);
    } else if (value instanceof EntityBean) {
      BeanDescriptor<?> d = getDescriptor(value.getClass());
      WriteJson writeJson = createWriteJson(gen, options);
      d.jsonWrite(writeJson, (EntityBean) value, null);
    } else {
      jsonScalar.write(gen, value);
    }
  }

  @Override
  public SpiJsonReader createJsonRead(BeanType<?> beanType, String json) {
    BeanDescriptor<?> desc = (BeanDescriptor<?>) beanType;
    JsonReader parser = createParser(json);
    return new ReadJson(desc, parser, null, defaultObjectMapper, false, stream());
  }

  @Override
  public SpiJsonWriter createJsonWriter(Writer writer) {
    return createJsonWriter(createGenerator(writer), null);
  }

  @Override
  public SpiJsonWriter createJsonWriter(JsonWriter gen, JsonWriteOptions options) {
    return createWriteJson(gen, options);
  }

  private WriteJson createWriteJson(JsonWriter gen, JsonWriteOptions options) {
    FetchPath pathProps = (options == null) ? null : options.getPathProperties();
    Map<String, JsonWriteBeanVisitor<?>> visitors = (options == null) ? null : options.getVisitorMap();
    return new WriteJson(server,
      gen,
      pathProps,
      visitors,
      determineObjectMapper(options),
      determineInclude(options),
      options == null || options.isIncludeLoadedImplicit());
  }

  private <T> void toJsonFromCollection(Collection<T> collection, String key, JsonWriter gen, JsonWriteOptions options) throws IOException {
    if (key != null) {
      gen.name(key);
    }
    gen.beginArray();
    WriteJson writeJson = createWriteJson(gen, options);
    for (T bean : collection) {
      if (bean == null) {
        gen.nullValue();
      } else if (bean instanceof EntityBean) {
        BeanDescriptor<?> d = getDescriptor(bean.getClass());
        d.jsonWrite(writeJson, (EntityBean) bean, null);
      } else {
        EJson.write(bean, gen);
      }
    }
    gen.endArray();
  }

  private void toJsonFromMap(Map<Object, Object> map, JsonWriter gen, JsonWriteOptions options) throws IOException {
    Set<Entry<Object, Object>> entrySet = map.entrySet();
    Iterator<Entry<Object, Object>> it = entrySet.iterator();

    WriteJson writeJson = createWriteJson(gen, options);
    gen.beginObject();

    while (it.hasNext()) {
      Entry<Object, Object> entry = it.next();
      String key = entry.getKey().toString();
      Object value = entry.getValue();
      if (value == null) {
        gen.name(key);
        gen.nullValue();
      } else if (value instanceof Collection<?>) {
        toJsonFromCollection((Collection<?>) value, key, gen, options);
      } else if (value instanceof EntityBean) {
        BeanDescriptor<?> d = getDescriptor(value.getClass());
        d.jsonWrite(writeJson, (EntityBean) value, key);
      } else {
        gen.name(key);
        EJson.write(value, gen);
      }
    }
    gen.endObject();
  }

  /**
   * Return the BeanDescriptor for the given bean type.
   */
  private <T> BeanDescriptor<T> getDescriptor(Class<T> beanType) {
    BeanDescriptor<T> d = server.descriptor(beanType);
    if (d == null) {
      throw new RuntimeException("No BeanDescriptor found for " + beanType);
    }
    return d;
  }

  /**
   * Determine the object mapper to use for a JSON read request.
   */
  private Object determineObjectMapper(JsonReadOptions options) {
    if (options == null) {
      return defaultObjectMapper;
    }
    Object mapper = options.getObjectMapper();
    return (mapper != null) ? mapper : defaultObjectMapper;
  }

  /**
   * Determine the object mapper to use for a JSON write request.
   */
  private Object determineObjectMapper(JsonWriteOptions options) {
    if (options == null) {
      return defaultObjectMapper;
    }
    Object mapper = options.getObjectMapper();
    return (mapper != null) ? mapper : defaultObjectMapper;
  }

  /**
   * Determine the include mode to use for a JSON write request.
   */
  private JsonConfig.Include determineInclude(JsonWriteOptions options) {
    if (options == null) {
      return defaultInclude;
    }
    JsonConfig.Include include = options.getInclude();
    return (include != null) ? include : defaultInclude;
  }

}
