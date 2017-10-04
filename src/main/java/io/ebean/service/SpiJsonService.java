package io.ebean.service;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * JSON service that Ebean is expected to provide.
 *
 * Supports converting between JSON content and simple java Maps/Lists.
 */
public interface SpiJsonService {

  /**
   * Write the nested Map/List as json.
   */
  String write(Object object) throws IOException;

  /**
   * Write the nested Map/List as json to the writer.
   */
  void write(Object object, Writer writer) throws IOException;

  /**
   * Write the nested Map/List as json to the jsonGenerator.
   */
  void write(Object object, JsonGenerator jsonGenerator) throws IOException;

  /**
   * Write the collection as json array to the jsonGenerator.
   */
  void writeCollection(Collection<Object> collection, JsonGenerator jsonGenerator) throws IOException;

  /**
   * Parse the json and return as a Map additionally specifying if the returned map should
   * be modify aware meaning that it can detect when it has been modified.
   */
  Map<String, Object> parseObject(String json, boolean modifyAware) throws IOException;

  /**
   * Parse the json and return as a Map.
   */
  Map<String, Object> parseObject(String json) throws IOException;

  /**
   * Parse the json and return as a Map taking a reader.
   */
  Map<String, Object> parseObject(Reader reader, boolean modifyAware) throws IOException;

  /**
   * Parse the json and return as a Map taking a reader.
   */
  Map<String, Object> parseObject(Reader reader) throws IOException;

  /**
   * Parse the json and return as a Map taking a JsonParser.
   */
  Map<String, Object> parseObject(JsonParser parser) throws IOException;

  /**
   * Parse the json and return as a Map taking a JsonParser and a starting token.
   * <p>
   * Used when the first token is checked to see if the value is null prior to calling this.
   * </p>
   */
  Map<String, Object> parseObject(JsonParser parser, JsonToken token) throws IOException;

  /**
   * Parse the json and return as a modify aware List.
   */
  <T> List<T> parseList(String json, boolean modifyAware) throws IOException;

  /**
   * Parse the json and return as a List.
   */
  List<Object> parseList(String json) throws IOException;

  /**
   * Parse the json and return as a List taking a Reader.
   */
  List<Object> parseList(Reader reader) throws IOException;

  /**
   * Parse the json and return as a List taking a JsonParser.
   */
  List<Object> parseList(JsonParser parser) throws IOException;

  /**
   * Parse the json returning as a List taking into account the current token.
   */
  <T> List<T> parseList(JsonParser parser, JsonToken currentToken) throws IOException;

  /**
   * Parse the json and return as a List or Map.
   */
  Object parse(String json) throws IOException;

  /**
   * Parse the json and return as a List or Map.
   */
  Object parse(Reader reader) throws IOException;

  /**
   * Parse the json and return as a List or Map.
   */
  Object parse(JsonParser parser) throws IOException;

  /**
   * Parse the json returning a Set that might be modify aware.
   */
  <T> Set<T> parseSet(String json, boolean modifyAware) throws IOException;

  /**
   * Parse the json returning as a Set taking into account the current token.
   */
  <T> Set<T> parseSet(JsonParser parser, JsonToken currentToken) throws IOException;
}
