package io.ebeaninternal.json;

import io.avaje.json.JsonReader;
import io.avaje.json.JsonReader.Token;
import io.avaje.json.stream.JsonStream;
import io.ebean.ModifyAwareType;

import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class EJsonReader {

  private EJsonReader() {
  }

  private static JsonReader reader(String content) {
    return JsonStream.builder().build().reader(content);
  }

  @SuppressWarnings("unchecked")
  static Map<String, Object> parseObject(String json, boolean modifyAware) throws IOException {
    return (Map<String, Object>) parse(json, modifyAware);
  }

  @SuppressWarnings("unchecked")
  static Map<String, Object> parseObject(String json) throws IOException {
    return (Map<String, Object>) parse(json);
  }

  @SuppressWarnings("unchecked")
  static Map<String, Object> parseObject(Reader reader) throws IOException {
    return (Map<String, Object>) parse(reader);
  }

  @SuppressWarnings("unchecked")
  static Map<String, Object> parseObject(Reader reader, boolean modifyAware) throws IOException {
    return (Map<String, Object>) parse(reader, modifyAware);
  }

  @SuppressWarnings("unchecked")
  static Map<String, Object> parseObject(JsonReader parser) throws IOException {
    return (Map<String, Object>) parse(parser);
  }

  @SuppressWarnings("unchecked")
  static Map<String, Object> parseObject(JsonReader parser, Token token) throws IOException {
    return (Map<String, Object>) parse(parser, token, false);
  }

  @SuppressWarnings("unchecked")
  static <T> List<T> parseList(String json, boolean modifyAware) throws IOException {
    return (List<T>) parse(json, modifyAware);
  }

  @SuppressWarnings("unchecked")
  static List<Object> parseList(String json) throws IOException {
    return (List<Object>) parse(json);
  }

  @SuppressWarnings("unchecked")
  static List<Object> parseList(Reader reader) throws IOException {
    return (List<Object>) parse(reader);
  }

  @SuppressWarnings("unchecked")
  static List<Object> parseList(JsonReader parser, boolean modifyAware) throws IOException {
    return (List<Object>) parse(parser, null, modifyAware);
  }

  static Object parse(String json) throws IOException {
    return parseRawJson(json, null);
  }

  static Object parse(String json, boolean modifyAware) throws IOException {
    return parseRawJson(json, modifyAware ? new ModifyAwareFlag() : null);
  }

  static Object parse(Reader reader) throws IOException {
    return parseRawJson(readAll(reader), null);
  }

  static Object parse(Reader reader, boolean modifyAware) throws IOException {
    return parseRawJson(readAll(reader), modifyAware ? new ModifyAwareFlag() : null);
  }

  static Object parse(JsonReader parser) throws IOException {
    return parse(parser, null, false);
  }

  static Object parse(JsonReader parser, boolean modifyAware) throws IOException {
    return parse(parser, null, modifyAware);
  }

  static Object parse(JsonReader parser, Token token, boolean modifyAware) throws IOException {
    ModifyAwareType owner = modifyAware ? new ModifyAwareFlag() : null;
    Token effectiveToken = token == null ? parser.currentToken() : token;
    if (effectiveToken == null) {
      return parseRawJson(parser.readRaw(), owner);
    }
    return parseValue(parser, effectiveToken, owner);
  }

  private static Object parseValue(JsonReader parser, Token token, ModifyAwareType owner) throws IOException {
    if (token == null) {
      token = parser.currentToken();
      if (token == null) {
        if (parser.isNullValue()) {
          return null;
        }
        return parseRawJson(parser.readRaw(), owner);
      }
    }
    if (token == Token.BEGIN_OBJECT) {
      return parseObjectValue(parser, owner);
    }
    if (token == Token.BEGIN_ARRAY) {
      return parseArrayValue(parser, owner);
    }
    if (token == Token.NUMBER) {
      BigDecimal value = parser.readDecimal();
      return value.scale() <= 0 ? value.longValue() : value;
    }
    if (token == Token.STRING) {
      return parser.readString();
    }
    if (token == Token.BOOLEAN) {
      return parser.readBoolean();
    }
    if (token == Token.NULL) {
      parser.isNullValue();
      return null;
    }
    return parseRawJson(parser.readRaw(), owner);
  }

  private static Object parseRawJson(String json, ModifyAwareType owner) throws IOException {
    if (json == null) {
      return null;
    }
    String content = json.trim();
    if (content.isEmpty()) {
      return null;
    }
    JsonReader parser = reader(content);
    Token token = parser.currentToken();
    return parseValue(parser, token, owner);
  }

  private static String readAll(Reader reader) throws IOException {
    StringBuilder builder = new StringBuilder();
    char[] buffer = new char[2048];
    int len;
    while ((len = reader.read(buffer)) != -1) {
      builder.append(buffer, 0, len);
    }
    return builder.toString();
  }

  private static Map<String, Object> parseObjectValue(JsonReader parser, ModifyAwareType owner) throws IOException {
    Map<String, Object> map = owner == null
      ? new LinkedHashMap<>()
      : new ModifyAwareMap<>(owner, new LinkedHashMap<>());

    parser.beginObject();
    while (parser.hasNextField()) {
      String fieldName = parser.nextField();
      map.put(fieldName, parseValue(parser, parser.currentToken(), owner));
      if (owner != null) {
        owner.setMarkedDirty(false);
      }
    }
    parser.endObject();
    if (owner != null) {
      owner.setMarkedDirty(false);
    }
    return map;
  }

  private static List<Object> parseArrayValue(JsonReader parser, ModifyAwareType owner) throws IOException {
    List<Object> list = owner == null
      ? new ArrayList<>()
      : new ModifyAwareList<>(owner, new ArrayList<>());

    parser.beginArray();
    while (parser.hasNextElement()) {
      Token elementToken = parser.currentToken();
      Object elementValue = parseValue(parser, elementToken, owner);
      list.add(elementValue);
      if (owner != null) {
        owner.setMarkedDirty(false);
      }
    }
    parser.endArray();
    if (owner != null) {
      owner.setMarkedDirty(false);
    }
    return list;
  }
}
