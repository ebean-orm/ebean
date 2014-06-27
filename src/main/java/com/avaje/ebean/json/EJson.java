package com.avaje.ebean.json;

import java.io.Reader;
import java.io.Writer;
import java.util.List;
import java.util.Map;

import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonParser;

public class EJson {

  public static String write(Object object) {
    return EJsonWriter.write(object);
  }
  
  public static void write(Object object, Writer writer) {
    EJsonWriter.write(object, writer);
  }
  
  public static void write(Object object, JsonGenerator jsonGenerator) {
    EJsonWriter.write(object, jsonGenerator);
  }
  
  public static Map<String,Object> parseObject(String json) {
    return EJsonReader.parseObject(json);
  }
  
  public static List<Object> parseList(String json) {
    return EJsonReader.parseList(json);
  }
  
  public static Object parse(String json) {
    return EJsonReader.parse(json);
  }
  
  public static Object parse(Reader reader) {
    return EJsonReader.parse(reader);
  }
  
  public static Object parse(JsonParser parser) {
    return EJsonReader.parse(parser);
  }
}
