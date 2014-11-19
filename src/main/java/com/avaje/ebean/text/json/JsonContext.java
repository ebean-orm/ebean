package com.avaje.ebean.text.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;

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
  public <T> T toBean(Class<T> rootType, String json) throws JsonIOException;

  /**
   * Convert json reader input into a Bean of a specific type.
   *
   * @throws JsonIOException When IOException occurs
   */
  public <T> T toBean(Class<T> rootType, Reader json) throws JsonIOException;

  /**
   * Convert json string input into a list of beans of a specific type.
   *
   * @throws JsonIOException When IOException occurs
   */
  public <T> List<T> toList(Class<T> rootType, String json) throws JsonIOException;

  /**
   * Convert json reader input into a list of beans of a specific type.
   *
   * @throws JsonIOException When IOException occurs
   */
  public <T> List<T> toList(Class<T> rootType, Reader json) throws JsonIOException;

  /**
   * Use the genericType to determine if this should be converted into a List or
   * bean.
   *
   * @throws JsonIOException When IOException occurs
   */
  public Object toObject(Type genericType, Reader json) throws JsonIOException;

  /**
   * Use the genericType to determine if this should be converted into a List or
   * bean.
   *
   * @throws JsonIOException When IOException occurs
   */
  public Object toObject(Type genericType, String json) throws JsonIOException;

  /**
   * Write the bean or collection in JSON format to the writer with default
   * options.
   *
   * @param value  the bean or collection of beans to write
   * @param writer used to write the json output to
   * @throws JsonIOException When IOException occurs
   */
  public void toJson(Object value, Writer writer) throws JsonIOException;

  /**
   * With additional options to specify JsonValueAdapter and
   * JsonWriteBeanVisitor's.
   *
   * @param value   the bean or collection of beans to write
   * @param writer  used to write the json output to
   * @param options additional options to control the JSON output
   * @throws JsonIOException When IOException occurs
   */
  public void toJson(Object value, Writer writer, JsonWriteOptions options) throws JsonIOException;

  /**
   * Convert a bean or collection to json string using default options.
   *
   * @throws JsonIOException When IOException occurs
   */
  public String toJson(Object value) throws JsonIOException;

  /**
   * Convert a bean or collection to json string.
   *
   * @throws JsonIOException When IOException occurs
   */
  public String toJson(Object value, JsonWriteOptions options) throws JsonIOException;

  /**
   * Return true if the type is known as an Entity bean or a List Set or
   * Map of entity beans.
   */
  public boolean isSupportedType(Type genericType);

  /**
   * Create and return a new JsonGenerator for the given writer.
   *
   * @throws JsonIOException When IOException occurs
   */
  public JsonGenerator createGenerator(Writer writer) throws JsonIOException;

  /**
   * Create and return a new JsonParser for the given reader.
   *
   * @throws JsonIOException When IOException occurs
   */
  public JsonParser createParser(Reader reader) throws JsonIOException;
}