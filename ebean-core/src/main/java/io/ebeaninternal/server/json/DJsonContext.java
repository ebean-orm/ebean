package io.ebeaninternal.server.json;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import io.ebean.FetchPath;
import io.ebean.bean.EntityBean;
import io.ebean.config.JsonConfig;
import io.ebean.plugin.BeanType;
import io.ebean.text.json.*;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.api.SpiJsonContext;
import io.ebeaninternal.api.json.SpiJsonReader;
import io.ebeaninternal.api.json.SpiJsonWriter;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.type.TypeManager;
import io.ebeaninternal.util.ParamTypeHelper;
import io.ebeaninternal.util.ParamTypeHelper.ManyType;
import io.ebeaninternal.util.ParamTypeHelper.TypeInfo;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;
import java.util.Map.Entry;

/**
 * Default implementation of JsonContext.
 */
public final class DJsonContext implements SpiJsonContext {

  private static final PrettyPrinter PRETTY_PRINTER = new Pretty();

  private final SpiEbeanServer server;
  private final JsonFactory jsonFactory;
  private final Object defaultObjectMapper;
  private final JsonConfig.Include defaultInclude;
  private final DJsonScalar jsonScalar;

  private static class Pretty extends DefaultPrettyPrinter {
    Pretty() {
      _objectFieldValueSeparatorWithSpaces = ": ";
    }
  }

  public DJsonContext(SpiEbeanServer server, JsonFactory jsonFactory, TypeManager typeManager) {
    this.server = server;
    this.jsonFactory = (jsonFactory != null) ? jsonFactory : new JsonFactory();
    this.defaultObjectMapper = this.server.config().getObjectMapper();
    this.defaultInclude = this.server.config().getJsonInclude();
    this.jsonScalar = new DJsonScalar(typeManager);
  }

  @Override
  public void writeScalar(JsonGenerator generator, Object scalarValue) throws IOException {
    jsonScalar.write(generator, scalarValue);
  }

  @Override
  public boolean isSupportedType(Type genericType) {
    return server.isSupportedType(genericType);
  }

  @Override
  public JsonGenerator createGenerator(Writer writer) throws JsonIOException {
    try {
      return jsonFactory.createGenerator(writer);
    } catch (IOException e) {
      throw new JsonIOException(e);
    }
  }

  @Override
  public JsonParser createParser(Reader reader) throws JsonIOException {
    try {
      return jsonFactory.createParser(reader);
    } catch (IOException e) {
      throw new JsonIOException(e);
    }
  }

  @Override
  public <T> T toBean(Class<T> cls, String json) throws JsonIOException {
    return toBean(cls, new StringReader(json));
  }

  @Override
  public <T> T toBean(Class<T> cls, String json, JsonReadOptions options) throws JsonIOException {
    return toBean(cls, new StringReader(json), options);
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
  public <T> T toBean(Class<T> cls, JsonParser parser) throws JsonIOException {
    return toBean(cls, parser, null);
  }

  @Override
  public <T> T toBean(Class<T> cls, JsonParser parser, JsonReadOptions options) throws JsonIOException {
    BeanDescriptor<T> desc = getDescriptor(cls);
    try {
      return desc.jsonRead(new ReadJson(desc, parser, options, determineObjectMapper(options)), null);
    } catch (IOException e) {
      throw new JsonIOException(e);
    }
  }

  @Override
  public <T> void toBean(T target, String json) throws JsonIOException {
    toBean(target, new StringReader(json));
  }

  @Override
  public <T> void toBean(T target, String json, JsonReadOptions readOptions) throws JsonIOException {
    toBean(target, new StringReader(json), readOptions);
  }

  @Override
  public <T> void toBean(T target, Reader jsonReader) throws JsonIOException {
    toBean(target, createParser(jsonReader));
  }

  @Override
  public <T> void toBean(T target, Reader jsonReader, JsonReadOptions readOptions) throws JsonIOException {
    toBean(target, createParser(jsonReader), readOptions);
  }

  @Override
  public <T> void toBean(T target, JsonParser parser) throws JsonIOException {
    toBean(target, parser, null);
  }

  @SuppressWarnings("unchecked")
  @Override
  @Deprecated
  public <T> void toBean(T target, JsonParser parser, JsonReadOptions readOptions) throws JsonIOException {
    BeanDescriptor<T> desc = (BeanDescriptor<T>) getDescriptor(target.getClass());
    try {
      T bean =  desc.jsonRead(new ReadJson(desc, parser, readOptions, determineObjectMapper(readOptions)), null);
      desc.mergeBeans((EntityBean) bean, (EntityBean) target, null);
    } catch (IOException e) {
      throw new JsonIOException(e);
    }
  }

  @Override
  public <T> DJsonBeanReader<T> createBeanReader(Class<T> cls, JsonParser parser, JsonReadOptions options) throws JsonIOException {
    BeanDescriptor<T> desc = getDescriptor(cls);
    return new DJsonBeanReader<>(desc, new ReadJson(desc, parser, options, determineObjectMapper(options)));
  }

  @Override
  public <T> DJsonBeanReader<T> createBeanReader(BeanType<T> beanType, JsonParser parser, JsonReadOptions options) throws JsonIOException {
    BeanDescriptor<T> desc = (BeanDescriptor<T>) beanType;
    SpiJsonReader readJson = new ReadJson(desc, parser, options, determineObjectMapper(options));
    return new DJsonBeanReader<>(desc, readJson);
  }

  @Override
  public <T> List<T> toList(Class<T> cls, String json) throws JsonIOException {
    return toList(cls, new StringReader(json));
  }

  @Override
  public <T> List<T> toList(Class<T> cls, String json, JsonReadOptions options) throws JsonIOException {
    return toList(cls, new StringReader(json), options);
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
  public <T> List<T> toList(Class<T> cls, JsonParser src) throws JsonIOException {
    return toList(cls, src, null);
  }

  @Override
  public <T> List<T> toList(Class<T> cls, JsonParser src, JsonReadOptions options) throws JsonIOException {
    BeanDescriptor<T> desc = getDescriptor(cls);
    SpiJsonReader readJson = new ReadJson(desc, src, options, determineObjectMapper(options));
    try {

      JsonToken currentToken = src.getCurrentToken();
      if (currentToken != JsonToken.START_ARRAY) {
        JsonToken event = src.nextToken();
        if (event != JsonToken.START_ARRAY) {
          throw new JsonParseException(src, "Expecting start_array event but got " + event);
        }
      }

      List<T> list = new ArrayList<>();
      do {
        // CHECKME: Should we update the list
        T bean = desc.jsonRead(readJson, null);
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

  @Override
  public Object toObject(Type genericType, String json) throws JsonIOException {
    return toObject(genericType, createParser(new StringReader(json)));
  }

  @Override
  public Object toObject(Type genericType, Reader json) throws JsonIOException {
    return toObject(genericType, createParser(json));
  }

  @Override
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
  public void toJson(Object value, JsonGenerator generator, FetchPath fetchPath) throws JsonIOException {
    // generator passed in so don't close it
    toJsonNoClose(value, generator, JsonWriteOptions.pathProperties(fetchPath));
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
    try (JsonGenerator gen = createGenerator(writer)) {
      if (pretty) {
        gen.setPrettyPrinter(PRETTY_PRINTER);
      }
      toJsonInternal(value, gen, options);
    } catch (IOException e) {
      throw new JsonIOException(e);
    }
    return writer.toString();
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

    } else {
      jsonScalar.write(gen, value);
    }
  }

  @Override
  public SpiJsonReader createJsonRead(BeanType<?> beanType, String json) {
    BeanDescriptor<?> desc = (BeanDescriptor<?>) beanType;
    JsonParser parser = createParser(new StringReader(json));
    return new ReadJson(desc, parser, null, defaultObjectMapper);
  }

  @Override
  public SpiJsonWriter createJsonWriter(Writer writer) {
    return createJsonWriter(createGenerator(writer), null);
  }

  @Override
  public SpiJsonWriter createJsonWriter(JsonGenerator gen, JsonWriteOptions options) {
    return createWriteJson(gen, options);
  }

  private WriteJson createWriteJson(JsonGenerator gen, JsonWriteOptions options) {
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
