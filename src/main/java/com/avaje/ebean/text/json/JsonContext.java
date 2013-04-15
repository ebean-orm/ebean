package com.avaje.ebean.text.json;

import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.List;

/**
 * Converts objects to and from JSON format.
 * 
 * @author rbygrave
 */
public interface JsonContext {

  /**
   * Convert json string input into a Bean of a specific type.
   */
  public <T> T toBean(Class<T> rootType, String json);

  /**
   * Convert json reader input into a Bean of a specific type.
   */
  public <T> T toBean(Class<T> rootType, Reader json);

  /**
   * Convert json string input into a Bean of a specific type with options.
   */
  public <T> T toBean(Class<T> rootType, String json, JsonReadOptions options);

  /**
   * Convert json reader input into a Bean of a specific type with options.
   */
  public <T> T toBean(Class<T> rootType, Reader json, JsonReadOptions options);

  /**
   * Convert json string input into a list of beans of a specific type.
   */
  public <T> List<T> toList(Class<T> rootType, String json);

  /**
   * Convert json string input into a list of beans of a specific type with
   * options.
   */
  public <T> List<T> toList(Class<T> rootType, String json, JsonReadOptions options);

  /**
   * Convert json reader input into a list of beans of a specific type.
   */
  public <T> List<T> toList(Class<T> rootType, Reader json);

  /**
   * Convert json reader input into a list of beans of a specific type with
   * options.
   */
  public <T> List<T> toList(Class<T> rootType, Reader json, JsonReadOptions options);

  /**
   * Use the genericType to determine if this should be converted into a List or
   * bean.
   */
  public Object toObject(Type genericType, Reader json, JsonReadOptions options);

  /**
   * Use the genericType to determine if this should be converted into a List or
   * bean.
   */
  public Object toObject(Type genericType, String json, JsonReadOptions options);

  /**
   * Write the bean or collection in JSON format to the writer with default
   * options.
   * 
   * @param o
   *          the bean or collection of beans to write
   * @param writer
   *          used to write the json output to
   */
  public void toJsonWriter(Object o, Writer writer);

  /**
   * With additional pretty output option.
   */
  public void toJsonWriter(Object o, Writer writer, boolean pretty);

  /**
   * With additional options to specify JsonValueAdapter and
   * JsonWriteBeanVisitor's.
   * 
   * @param o
   *          the bean or collection of beans to write
   * @param writer
   *          used to write the json output to
   * @param options
   *          additional options to control the JSON output
   */
  public void toJsonWriter(Object o, Writer writer, boolean pretty, JsonWriteOptions options);

  /**
   * With additional JSONP callback function.
   */
  public void toJsonWriter(Object o, Writer writer, boolean pretty, JsonWriteOptions options,
      String callback);

  /**
   * Convert a bean or collection to json string using default options.
   */
  public String toJsonString(Object o);

  /**
   * Convert a bean or collection to json string with pretty format using
   * default options.
   */
  public String toJsonString(Object o, boolean pretty);

  /**
   * Convert a bean or collection to json string using options.
   */
  public String toJsonString(Object o, boolean pretty, JsonWriteOptions options);

  /**
   * Convert a bean or collection to json string using a JSONP callback.
   */
  public String toJsonString(Object o, boolean pretty, JsonWriteOptions options, String callback);

  /**
   * Return true if the type is known as an Entity or Xml type or a List Set or
   * Map of known bean types.
   */
  public boolean isSupportedType(Type genericType);

}