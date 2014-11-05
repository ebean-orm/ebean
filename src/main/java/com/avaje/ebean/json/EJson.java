package com.avaje.ebean.json;

import java.io.Reader;
import java.io.Writer;
import java.util.List;
import java.util.Map;

import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonParser;

/**
 * Utility that converts between JSON content and java Maps/Lists.
 */
public class EJson {

  /**
   * Write the nested Map/List as json.
   */
  public static String write(Object object) {
    return EJsonWriter.write(object);
  }
  
  /**
   * Write the nested Map/List as json to the writer.
   */
  public static void write(Object object, Writer writer) {
    EJsonWriter.write(object, writer);
  }
  
  /**
   * Write the nested Map/List as json to the jsonGenerator.
   */
  public static void write(Object object, JsonGenerator jsonGenerator) {
    EJsonWriter.write(object, jsonGenerator);
  }
  
  /**
   * Parse the json and return as a Map.
   */
  public static Map<String,Object> parseObject(String json) {
    return EJsonReader.parseObject(json);
  }
  
  /**
   * Parse the json and return as a Map taking a reader.
   */
  public static Map<String,Object> parseObject(Reader reader) {
    return EJsonReader.parseObject(reader);
  }
  
  /**
   * Parse the json and return as a Map taking a JsonParser.
   */
  public static Map<String,Object> parseObject(JsonParser parser) {
    return EJsonReader.parseObject(parser);
  }
  
  /**
   * Parse the json and return as a List.
   */
  public static List<Object> parseList(String json) {
    return EJsonReader.parseList(json);
  }
  
  /**
   * Parse the json and return as a List taking a Reader.
   */
  public static List<Object> parseList(Reader reader) {
    return EJsonReader.parseList(reader);
  }
  
  /**
   * Parse the json and return as a List taking a JsonParser.
   */
  public static List<Object> parseList(JsonParser parser) {
    return EJsonReader.parseList(parser);
  }
  
  
  /**
   * Parse the json and return as a List or Map.
   */
  public static Object parse(String json) {
    return EJsonReader.parse(json);
  }
  
  /**
   * Parse the json and return as a List or Map.
   */
  public static Object parse(Reader reader) {
    return EJsonReader.parse(reader);
  }
  
  /**
   * Parse the json and return as a List or Map.
   */
  public static Object parse(JsonParser parser) {
    return EJsonReader.parse(parser);
  }
}
