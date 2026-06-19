package io.ebeaninternal.json;

import io.avaje.json.JsonAdapter;
import io.avaje.json.JsonReader;
import io.avaje.json.JsonReader.Token;
import io.avaje.json.JsonWriter;
import io.avaje.json.stream.JsonStream;
import io.ebean.ModifyAwareType;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Ebean specific {@link JsonAdapter} that materializes JSON into plain Java
 * Map/List/scalar values - optionally wrapped in modify-aware collections so
 * that mutations after load are tracked as dirty.
 * <p>
 * This consolidates the prior EJsonReader/EJsonWriter behavior into a single
 * adapter that plugs into avaje {@code JsonMapper}.
 */
final class EbeanJsonAdapter implements JsonAdapter<Object> {

  static final EbeanJsonAdapter PLAIN = new EbeanJsonAdapter(false);
  static final EbeanJsonAdapter MODIFY_AWARE = new EbeanJsonAdapter(true);

  private static final JsonStream JSON_STREAM = JsonStream.builder().build();

  private final boolean modifyAware;

  private EbeanJsonAdapter(boolean modifyAware) {
    this.modifyAware = modifyAware;
  }

  @Override
  public Object fromJson(JsonReader reader) {
    return read(reader, null, modifyAware);
  }

  @Override
  public void toJson(JsonWriter writer, Object value) {
    write(writer, value);
  }

  /**
   * Read a value honoring an explicitly supplied current token (or current token when null).
   */
  static Object read(JsonReader parser, Token token, boolean modifyAware) {
    ModifyAwareType owner = modifyAware ? new ModifyAwareFlag() : null;
    Token effectiveToken = token == null ? parser.currentToken() : token;
    Object value;
    if (effectiveToken == null) {
      value = parseRawJson(parser.readRaw(), owner);
    } else {
      value = parseValue(parser, effectiveToken, owner);
    }
    if (owner != null) {
      owner.setMarkedDirty(false);
    }
    return value;
  }

  private static Object parseValue(JsonReader parser, Token token, ModifyAwareType owner) {
    if (token == null) {
      token = parser.currentToken();
      if (token == null) {
        if (parser.isNullValue()) {
          return null;
        }
        return parseRawJson(parser.readRaw(), owner);
      }
    }
    switch (token) {
      case BEGIN_OBJECT:
        return parseObjectValue(parser, owner);
      case BEGIN_ARRAY:
        return parseArrayValue(parser, owner);
      case NUMBER:
        BigDecimal value = parser.readDecimal();
        return value.scale() <= 0 ? value.longValue() : value;
      case STRING:
        return parser.readString();
      case BOOLEAN:
        return parser.readBoolean();
      case NULL:
        parser.isNullValue();
        return null;
      default:
        return parseRawJson(parser.readRaw(), owner);
    }
  }

  private static Object parseRawJson(String json, ModifyAwareType owner) {
    if (json == null) {
      return null;
    }
    String content = json.trim();
    if (content.isEmpty()) {
      return null;
    }
    try (JsonReader parser = JSON_STREAM.reader(content)) {
      return parseValue(parser, parser.currentToken(), owner);
    }
  }

  private static Map<String, Object> parseObjectValue(JsonReader parser, ModifyAwareType owner) {
    Map<String, Object> map = owner == null
      ? new LinkedHashMap<>()
      : new ModifyAwareMap<>(owner, new LinkedHashMap<>());

    parser.beginObject();
    while (parser.hasNextField()) {
      String fieldName = parser.nextField();
      map.put(fieldName, parseValue(parser, parser.currentToken(), owner));
    }
    parser.endObject();
    return map;
  }

  private static List<Object> parseArrayValue(JsonReader parser, ModifyAwareType owner) {
    List<Object> list = owner == null
      ? new ArrayList<>()
      : new ModifyAwareList<>(owner, new ArrayList<>());

    parser.beginArray();
    while (parser.hasNextElement()) {
      list.add(parseValue(parser, parser.currentToken(), owner));
    }
    parser.endArray();
    return list;
  }

  /**
   * Write the value to an existing JsonWriter (used for the raw stream paths).
   */
  static void write(JsonWriter jsonWriter, Object object) {
    if (object == null) {
      jsonWriter.nullValue();
    } else if (object instanceof String) {
      jsonWriter.value((String) object);
    } else if (object instanceof Integer) {
      jsonWriter.value((Integer) object);
    } else if (object instanceof Long) {
      jsonWriter.value((Long) object);
    } else if (object instanceof Double) {
      jsonWriter.value((Double) object);
    } else if (object instanceof Float) {
      jsonWriter.value((Float) object);
    } else if (object instanceof BigDecimal) {
      jsonWriter.value((BigDecimal) object);
    } else if (object instanceof Boolean) {
      jsonWriter.value((Boolean) object);
    } else if (object instanceof Map<?, ?>) {
      writeMap(jsonWriter, (Map<?, ?>) object);
    } else if (object instanceof Collection<?>) {
      writeCollection(jsonWriter, (Collection<?>) object);
    } else {
      jsonWriter.value(object.toString());
    }
  }

  private static void writeMap(JsonWriter jsonWriter, Map<?, ?> map) {
    jsonWriter.beginObject();
    for (Map.Entry<?, ?> entry : map.entrySet()) {
      jsonWriter.name((String) entry.getKey());
      write(jsonWriter, entry.getValue());
    }
    jsonWriter.endObject();
  }

  static void writeCollection(JsonWriter jsonWriter, Collection<?> collection) {
    jsonWriter.beginArray();
    for (Object element : collection) {
      write(jsonWriter, element);
    }
    jsonWriter.endArray();
  }
}
