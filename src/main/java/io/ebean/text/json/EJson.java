package io.ebean.text.json;

import io.ebeaninternal.server.type.ModifyAwareList;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Utility that converts between JSON content and simple java Maps/Lists.
 */
public class EJson {

  /**
   * Write the nested Map/List as json.
   */
  public static String write(Object object) throws IOException {
    return EJsonWriter.write(object);
  }

  /**
   * Write the nested Map/List as json to the writer.
   */
  public static void write(Object object, Writer writer) throws IOException {
    EJsonWriter.write(object, writer);
  }

  /**
   * Write the nested Map/List as json to the jsonGenerator.
   */
  public static void write(Object object, JsonGenerator jsonGenerator) throws IOException {
    EJsonWriter.write(object, jsonGenerator);
  }

  /**
   * Write the collection as json array to the jsonGenerator.
   */
  public static void writeCollection(Collection<Object> collection, JsonGenerator jsonGenerator) throws IOException {
    EJsonWriter.writeCollection(collection, jsonGenerator);
  }

  /**
   * Parse the json and return as a Map additionally specifying if the returned map should
   * be modify aware meaning that it can detect when it has been modified.
   */
  public static Map<String, Object> parseObject(String json, boolean modifyAware) throws IOException {
    return EJsonReader.parseObject(json, modifyAware);
  }

  /**
   * Parse the json and return as a Map.
   */
  public static Map<String, Object> parseObject(String json) throws IOException {
    return EJsonReader.parseObject(json);
  }

  /**
   * Parse the json and return as a Map taking a reader.
   */
  public static Map<String, Object> parseObject(Reader reader, boolean modifyAware) throws IOException {
    return EJsonReader.parseObject(reader, modifyAware);
  }

  /**
   * Parse the json and return as a Map taking a reader.
   */
  public static Map<String, Object> parseObject(Reader reader) throws IOException {
    return EJsonReader.parseObject(reader);
  }

  /**
   * Parse the json and return as a Map taking a JsonParser.
   */
  public static Map<String, Object> parseObject(JsonParser parser) throws IOException {
    return EJsonReader.parseObject(parser);
  }

  /**
   * Parse the json and return as a Map taking a JsonParser and a starting token.
   * <p>
   * Used when the first token is checked to see if the value is null prior to calling this.
   * </p>
   */
  public static Map<String, Object> parseObject(JsonParser parser, JsonToken token) throws IOException {
    return EJsonReader.parseObject(parser, token);
  }

  /**
   * Parse the json and return as a modify aware List.
   */
  public static <T> List<T> parseList(String json, boolean modifyAware) throws IOException {
    return EJsonReader.parseList(json, modifyAware);
  }

  /**
   * Parse the json and return as a List.
   */
  public static List<Object> parseList(String json) throws IOException {
    return EJsonReader.parseList(json);
  }

  /**
   * Parse the json and return as a List taking a Reader.
   */
  public static List<Object> parseList(Reader reader) throws IOException {
    return EJsonReader.parseList(reader);
  }

  /**
   * Parse the json and return as a List taking a JsonParser.
   */
  public static List<Object> parseList(JsonParser parser) throws IOException {
    return EJsonReader.parseList(parser, false);
  }

  /**
   * Parse the json returning as a List taking into account the current token.
   */
  @SuppressWarnings("unchecked")
  public static <T> List<T> parseList(JsonParser parser, JsonToken currentToken) throws IOException {
    return (List<T>) EJsonReader.parse(parser, currentToken, false);
  }

  /**
   * Parse the json and return as a List or Map.
   */
  public static Object parse(String json) throws IOException {
    return EJsonReader.parse(json);
  }

  /**
   * Parse the json and return as a List or Map.
   */
  public static Object parse(Reader reader) throws IOException {
    return EJsonReader.parse(reader);
  }

  /**
   * Parse the json and return as a List or Map.
   */
  public static Object parse(JsonParser parser) throws IOException {
    return EJsonReader.parse(parser);
  }

  /**
   * Parse the json returning a Set that might be modify aware.
   */
  public static <T> Set<T> parseSet(String json, boolean modifyAware) throws IOException {
    List<T> list = parseList(json, modifyAware);
    if (list == null) {
      return null;
    }

    if (modifyAware) {
      return ((ModifyAwareList<T>) list).asSet();
    } else {
      return new LinkedHashSet<T>(list);
    }
  }

  /**
   * Parse the json returning as a Set taking into account the current token.
   */
  public static <T> Set<T> parseSet(JsonParser parser, JsonToken currentToken) throws IOException {
    return new LinkedHashSet<T>(parseList(parser, currentToken));
  }
}
