package io.ebean.text.json;

import io.ebean.FetchPath;
import io.ebean.plugin.BeanType;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.List;

/**
 * Converts objects to and from JSON format.
 */
public interface JsonContext {

  /**
   * Convert json string input into a Bean of a specific type.
   *
   * @throws JsonIOException When IOException occurs
   */
  <T> T toBean(Class<T> rootType, String json) throws JsonIOException;

  /**
   * Convert json string input into a Bean of a specific type additionally using JsonReadOptions.
   *
   * @throws JsonIOException When IOException occurs
   */
  <T> T toBean(Class<T> rootType, String json, JsonReadOptions options) throws JsonIOException;

  /**
   * Convert json reader input into a Bean of a specific type.
   *
   * @throws JsonIOException When IOException occurs
   */
  <T> T toBean(Class<T> rootType, Reader json) throws JsonIOException;

  /**
   * Convert json reader input into a Bean of a specific type additionally using JsonReadOptions.
   *
   * @throws JsonIOException When IOException occurs
   */
  <T> T toBean(Class<T> rootType, Reader json, JsonReadOptions options) throws JsonIOException;

  /**
   * Convert json parser input into a Bean of a specific type.
   *
   * @throws JsonIOException When IOException occurs
   */
  <T> T toBean(Class<T> cls, JsonParser parser) throws JsonIOException;

  /**
   * Convert json parser input into a Bean of a specific type additionally using JsonReadOptions..
   *
   * @throws JsonIOException When IOException occurs
   */
  <T> T toBean(Class<T> cls, JsonParser parser, JsonReadOptions options) throws JsonIOException;

  /**
   * Create and return a new bean reading for the bean type given the JSON options and source.
   * <p>
   * Note that JsonOption provides an option for setting a persistence context and also enabling
   * further lazy loading. Further lazy loading requires a persistence context so if that is set
   * on then a persistence context is created if there is not one set.
   */
  <T> JsonBeanReader<T> createBeanReader(Class<T> cls, JsonParser parser, JsonReadOptions options) throws JsonIOException;

  /**
   * Create and return a new bean reading for the bean type given the JSON options and source.
   * <p>
   * Note that JsonOption provides an option for setting a persistence context and also enabling
   * further lazy loading. Further lazy loading requires a persistence context so if that is set
   * on then a persistence context is created if there is not one set.
   */
  <T> JsonBeanReader<T> createBeanReader(BeanType<T> beanType, JsonParser parser, JsonReadOptions options) throws JsonIOException;

  /**
   * Convert json string input into a list of beans of a specific type.
   *
   * @throws JsonIOException When IOException occurs
   */
  <T> List<T> toList(Class<T> rootType, String json) throws JsonIOException;

  /**
   * Convert json string input into a list of beans of a specific type additionally using JsonReadOptions.
   *
   * @throws JsonIOException When IOException occurs
   */
  <T> List<T> toList(Class<T> rootType, String json, JsonReadOptions options) throws JsonIOException;

  /**
   * Convert json reader input into a list of beans of a specific type.
   *
   * @throws JsonIOException When IOException occurs
   */
  <T> List<T> toList(Class<T> rootType, Reader json) throws JsonIOException;

  /**
   * Convert json reader input into a list of beans of a specific type additionally using JsonReadOptions.
   *
   * @throws JsonIOException When IOException occurs
   */
  <T> List<T> toList(Class<T> rootType, Reader json, JsonReadOptions options) throws JsonIOException;

  /**
   * Convert json parser input into a list of beans of a specific type.
   *
   * @throws JsonIOException When IOException occurs
   */
  <T> List<T> toList(Class<T> cls, JsonParser json) throws JsonIOException;

  /**
   * Convert json parser input into a list of beans of a specific type additionally using JsonReadOptions.
   *
   * @throws JsonIOException When IOException occurs
   */
  <T> List<T> toList(Class<T> cls, JsonParser json, JsonReadOptions options) throws JsonIOException;

  /**
   * Use the genericType to determine if this should be converted into a List or
   * bean.
   *
   * @throws JsonIOException When IOException occurs
   */
  Object toObject(Type genericType, Reader json) throws JsonIOException;

  /**
   * Use the genericType to determine if this should be converted into a List or
   * bean.
   *
   * @throws JsonIOException When IOException occurs
   */
  Object toObject(Type genericType, String json) throws JsonIOException;

  /**
   * Use the genericType to determine if this should be converted into a List or
   * bean.
   *
   * @throws JsonIOException When IOException occurs
   */
  Object toObject(Type genericType, JsonParser jsonParser) throws JsonIOException;

  /**
   * Return the bean or collection as JSON string.
   *
   * @throws JsonIOException When IOException occurs
   */
  String toJson(Object value) throws JsonIOException;

  /**
   * Write the bean or collection in JSON format to the writer.
   *
   * @throws JsonIOException When IOException occurs
   */
  void toJson(Object value, Writer writer) throws JsonIOException;

  /**
   * Write the bean or collection to the JsonGenerator.
   *
   * @throws JsonIOException When IOException occurs
   */
  void toJson(Object value, JsonGenerator generator) throws JsonIOException;

  /**
   * Return the bean or collection as JSON string using FetchPath.
   *
   * @throws JsonIOException When IOException occurs
   */
  String toJson(Object value, FetchPath fetchPath) throws JsonIOException;

  /**
   * Write the bean or collection as json to the writer using the FetchPath.
   */
  void toJson(Object value, Writer writer, FetchPath fetchPath) throws JsonIOException;

  /**
   * Write the bean or collection to the JsonGenerator using the FetchPath.
   */
  void toJson(Object value, JsonGenerator generator, FetchPath fetchPath) throws JsonIOException;

  /**
   * Deprecated in favour of using PathProperties by itself.
   * Write json to the JsonGenerator using the JsonWriteOptions.
   */
  void toJson(Object value, JsonGenerator generator, JsonWriteOptions options) throws JsonIOException;

  /**
   * Deprecated in favour of using PathProperties by itself.
   * With additional options.
   *
   * @throws JsonIOException When IOException occurs
   */
  void toJson(Object value, Writer writer, JsonWriteOptions options) throws JsonIOException;

  /**
   * Deprecated in favour of using PathProperties by itself.
   * Convert a bean or collection to json string.
   *
   * @throws JsonIOException When IOException occurs
   */
  String toJson(Object value, JsonWriteOptions options) throws JsonIOException;

  /**
   * Return true if the type is known as an Entity bean or a List Set or
   * Map of entity beans.
   */
  boolean isSupportedType(Type genericType);

  /**
   * Create and return a new JsonGenerator for the given writer.
   *
   * @throws JsonIOException When IOException occurs
   */
  JsonGenerator createGenerator(Writer writer) throws JsonIOException;

  /**
   * Create and return a new JsonParser for the given reader.
   *
   * @throws JsonIOException When IOException occurs
   */
  JsonParser createParser(Reader reader) throws JsonIOException;

  /**
   * Write a scalar types known to Ebean to Jackson.
   * <p>
   * Ebean has built in support for java8 and Joda types as well as the other
   * standard JDK types like URI, URL, UUID etc. This is a fast simple way to
   * write any of those types to Jackson.
   * </p>
   */
  void writeScalar(JsonGenerator generator, Object scalarValue) throws IOException;

}
