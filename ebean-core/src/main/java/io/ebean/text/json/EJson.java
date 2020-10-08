package io.ebean.text.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import io.ebean.service.SpiJsonService;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

/**
 * Utility that converts between JSON content and simple java Maps/Lists.
 */
public class EJson {

  private static SpiJsonService plugin = init();

  private static SpiJsonService init() {

    Iterator<SpiJsonService> loader = ServiceLoader.load(SpiJsonService.class).iterator();
    if (loader.hasNext()) {
      return loader.next();
    }
    throw new IllegalStateException("No service implementation found for SpiJsonService?");
  }

  /**
   * Write the nested Map/List as json.
   */
  public static String write(Object object) throws IOException {
    return plugin.write(object);
  }

  /**
   * Write the nested Map/List as json to the writer.
   */
  public static void write(Object object, Writer writer) throws IOException {
    plugin.write(object, writer);
  }

  /**
   * Write the nested Map/List as json to the jsonGenerator.
   */
  public static void write(Object object, JsonGenerator jsonGenerator) throws IOException {
    plugin.write(object, jsonGenerator);
  }

  /**
   * Write the collection as json array to the jsonGenerator.
   */
  public static void writeCollection(Collection<Object> collection, JsonGenerator jsonGenerator) throws IOException {
    plugin.writeCollection(collection, jsonGenerator);
  }

  /**
   * Parse the json and return as a Map additionally specifying if the returned map should
   * be modify aware meaning that it can detect when it has been modified.
   */
  public static Map<String, Object> parseObject(String json, boolean modifyAware) throws IOException {
    return plugin.parseObject(json, modifyAware);
  }

  /**
   * Parse the json and return as a Map.
   */
  public static Map<String, Object> parseObject(String json) throws IOException {
    return plugin.parseObject(json);
  }

  /**
   * Parse the json and return as a Map taking a reader.
   */
  public static Map<String, Object> parseObject(Reader reader, boolean modifyAware) throws IOException {
    return plugin.parseObject(reader, modifyAware);
  }

  /**
   * Parse the json and return as a Map taking a reader.
   */
  public static Map<String, Object> parseObject(Reader reader) throws IOException {
    return plugin.parseObject(reader);
  }

  /**
   * Parse the json and return as a Map taking a JsonParser.
   */
  public static Map<String, Object> parseObject(JsonParser parser) throws IOException {
    return plugin.parseObject(parser);
  }

  /**
   * Parse the json and return as a Map taking a JsonParser and a starting token.
   * <p>
   * Used when the first token is checked to see if the value is null prior to calling this.
   * </p>
   */
  public static Map<String, Object> parseObject(JsonParser parser, JsonToken token) throws IOException {
    return plugin.parseObject(parser, token);
  }

  /**
   * Parse the json and return as a modify aware List.
   */
  public static <T> List<T> parseList(String json, boolean modifyAware) throws IOException {
    return plugin.parseList(json, modifyAware);
  }

  /**
   * Parse the json and return as a List.
   */
  public static List<Object> parseList(String json) throws IOException {
    return plugin.parseList(json);
  }

  /**
   * Parse the json and return as a List taking a Reader.
   */
  public static List<Object> parseList(Reader reader) throws IOException {
    return plugin.parseList(reader);
  }

  /**
   * Parse the json and return as a List taking a JsonParser.
   */
  public static List<Object> parseList(JsonParser parser) throws IOException {
    return plugin.parseList(parser);
  }

  /**
   * Parse the json returning as a List taking into account the current token.
   */
  public static <T> List<T> parseList(JsonParser parser, JsonToken currentToken) throws IOException {
    return plugin.parseList(parser, currentToken);
  }

  /**
   * Parse the json and return as a List or Map.
   */
  public static Object parse(String json) throws IOException {
    return plugin.parse(json);
  }

  /**
   * Parse the json and return as a List or Map.
   */
  public static Object parse(Reader reader) throws IOException {
    return plugin.parse(reader);
  }

  /**
   * Parse the json and return as a List or Map.
   */
  public static Object parse(JsonParser parser) throws IOException {
    return plugin.parse(parser);
  }

  /**
   * Parse the json returning a Set that might be modify aware.
   */
  public static <T> Set<T> parseSet(String json, boolean modifyAware) throws IOException {
    return plugin.parseSet(json, modifyAware);
  }

  /**
   * Parse the json returning as a Set taking into account the current token.
   */
  public static <T> Set<T> parseSet(JsonParser parser, JsonToken currentToken) throws IOException {
    return plugin.parseSet(parser, currentToken);
  }
}
