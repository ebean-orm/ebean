package io.ebeaninternal.json;

import io.avaje.json.stream.JsonStream;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

/**
 * Utility to write simple java Maps/Lists as JSON.
 */
final class EJsonWriter {

  private static final JsonStream JSON_STREAM = JsonStream.builder().build();

  private EJsonWriter() {
  }

  /**
   * Convert object to Json content.
   */
  static String write(Object object) throws IOException {
    StringWriter writer = new StringWriter();
    write(object, writer);
    return writer.toString();
  }

  /**
   * Convert object to Json content.
   */
  static io.avaje.json.JsonWriter write(Object object, Writer writer) throws IOException {
    io.avaje.json.JsonWriter jsonWriter = JSON_STREAM.writer(writer);
    jsonWriter.serializeNulls(true);
    write(object, jsonWriter);
    jsonWriter.flush();
    return jsonWriter;
  }

  /**
   * Convert object to Json content.
   */
  static void write(Object object, io.avaje.json.JsonWriter jsonWriter) throws IOException {
    if (object == null) {
      jsonWriter.nullValue();
      return;
    }
    if (object instanceof String) {
      jsonWriter.value((String) object);
      return;
    }
    if (object instanceof Integer) {
      jsonWriter.value((Integer) object);
      return;
    }
    if (object instanceof Long) {
      jsonWriter.value((Long) object);
      return;
    }
    if (object instanceof Double) {
      jsonWriter.value((Double) object);
      return;
    }
    if (object instanceof Float) {
      jsonWriter.value((Float) object);
      return;
    }
    if (object instanceof BigDecimal) {
      jsonWriter.value((BigDecimal) object);
      return;
    }
    if (object instanceof Boolean) {
      jsonWriter.value((Boolean) object);
      return;
    }
    if (object instanceof Map<?, ?>) {
      writeMap((Map<?, ?>) object, jsonWriter);
      return;
    }
    if (object instanceof Collection<?>) {
      writeCollection((Collection<?>) object, jsonWriter);
      return;
    }

    jsonWriter.value(object.toString());
  }

  /**
   * Write map as Json content.
   */
  private static void writeMap(Map<?, ?> map, io.avaje.json.JsonWriter jsonWriter) throws IOException {
    jsonWriter.beginObject();
    for (Map.Entry<?, ?> entry : map.entrySet()) {
      String fieldName = (String) entry.getKey();
      Object value = entry.getValue();
      jsonWriter.name(fieldName);
      write(value, jsonWriter);
    }
    jsonWriter.endObject();
  }

  /**
   * Write list as Json content.
   */
  static void writeCollection(Collection<?> list, io.avaje.json.JsonWriter jsonWriter) throws IOException {
    jsonWriter.beginArray();
    for (Object element : list) {
      write(element, jsonWriter);
    }
    jsonWriter.endArray();
  }

  /**
   * Convert map to Json content.
   */
  static String write(Map<String, Object> map) throws IOException {
    StringWriter writer = new StringWriter();
    write(map, writer);
    return writer.toString();
  }

  /**
   * Convert map to Json content.
   */
  static io.avaje.json.JsonWriter write(Map<String, Object> map, Writer writer) throws IOException {
    io.avaje.json.JsonWriter jsonWriter = JSON_STREAM.writer(writer);
    jsonWriter.serializeNulls(true);
    write(map, jsonWriter);
    jsonWriter.flush();
    return jsonWriter;
  }

  /**
   * Convert map to Json content.
   */
  static void write(Map<String, Object> map, io.avaje.json.JsonWriter jsonWriter) throws IOException {
    jsonWriter.beginObject();
    Iterator<Map.Entry<String, Object>> it = map.entrySet().iterator();
    while (it.hasNext()) {
      Map.Entry<String, Object> entry = it.next();
      jsonWriter.name(entry.getKey());
      write(entry.getValue(), jsonWriter);
    }
    jsonWriter.endObject();
  }
}
