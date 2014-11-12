package com.avaje.ebean.text.json;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.List;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;

/**
 * Converts objects to and from JSON format.
 */
public interface JsonContext {

  /**
   * Convert json string input into a Bean of a specific type.
   */
  public <T> T toBean(Class<T> rootType, String json) throws IOException;

  /**
   * Convert json reader input into a Bean of a specific type.
   */
  public <T> T toBean(Class<T> rootType, Reader json) throws IOException;

  /**
   * Convert json string input into a list of beans of a specific type.
   */
  public <T> List<T> toList(Class<T> rootType, String json) throws IOException;

  /**
   * Convert json reader input into a list of beans of a specific type.
   * @throws IOException 
   */
  public <T> List<T> toList(Class<T> rootType, Reader json) throws IOException;

  /**
   * Use the genericType to determine if this should be converted into a List or
   * bean.
   */
  public Object toObject(Type genericType, Reader json) throws IOException;

  /**
   * Use the genericType to determine if this should be converted into a List or
   * bean.
   */
  public Object toObject(Type genericType, String json) throws IOException;

  /**
   * Write the bean or collection in JSON format to the writer with default
   * options.
   * 
   * @param value
   *          the bean or collection of beans to write
   * @param writer
   *          used to write the json output to
   */
  public void toJson(Object value, Writer writer) throws IOException;

  /**
   * With additional options to specify JsonValueAdapter and
   * JsonWriteBeanVisitor's.
   * 
   * @param value
   *          the bean or collection of beans to write
   * @param writer
   *          used to write the json output to
   * @param options
   *          additional options to control the JSON output
   */
  public void toJson(Object value, Writer writer, JsonWriteOptions options) throws IOException;

  /**
   * Convert a bean or collection to json string using default options.
   */
  public String toJson(Object value) throws IOException;

  /**
   * Convert a bean or collection to json string.
   */
  public String toJson(Object value, JsonWriteOptions options) throws IOException;

  /**
   * Return true if the type is known as an Entity bean or a List Set or
   * Map of entity beans.
   */
  public boolean isSupportedType(Type genericType);

  /**
   * Create and return a new JsonGenerator for the given writer.
   */
  public JsonGenerator createGenerator(Writer writer) throws IOException;
  
  /**
   * Create and return a new JsonParser for the given reader.
   */
  public JsonParser createParser(Reader reader) throws IOException;
}