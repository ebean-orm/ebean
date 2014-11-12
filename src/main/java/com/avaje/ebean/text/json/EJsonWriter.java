package com.avaje.ebean.text.json;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

class EJsonWriter {

  /**
   * Base jsonFactory implementation used when it is not passed in.
   */
  static JsonFactory jsonFactory = new JsonFactory();

  static String write(Object object) throws IOException {
    StringWriter writer = new StringWriter(200);
    write(object, writer);
    return writer.toString();
  }

  static void write(Object object, Writer writer) throws IOException {
    JsonGenerator generator = jsonFactory.createGenerator(writer);
    write(object, generator);
    generator.close();
  }

  static void write(Object object, JsonGenerator jsonGenerator) {
    new EJsonWriter(jsonGenerator).writeJson(object);
  }

  private final JsonGenerator jsonGenerator;

  private EJsonWriter(JsonGenerator jsonGenerator) {
    this.jsonGenerator = jsonGenerator;
  }

  private void writeJson(Object object) {
    writeJson(null, object);
  }

  @SuppressWarnings("unchecked")
  private void writeJson(String name, Object object) {
    try {
      if (object == null) {
        writeNull(name);

      } else if (object instanceof Map) {
        writeMap(name, (Map<Object, Object>) object);

      } else if (object instanceof Collection) {
        writeCollection(name, (Collection<Object>) object);

      } else if (object instanceof Boolean) {
        writeBoolean(name, (Boolean) object);

      } else if (object instanceof Number) {
        writeNumber(name, (Number) object);

      } else if (object instanceof Date) {
        writeDate(name, (Date) object);

      } else if (object instanceof String) {
        writeString(name, (String) object);

      } else if (object instanceof Map.Entry<?, ?>) {
        Map.Entry<?, ?> entry = (Map.Entry<?, ?>) object;
        writeJson(entry.getKey().toString(), entry.getValue());

      } else {
        writeString(name, object.toString());
      }

    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void writeBoolean(String name, Boolean object) throws IOException {
    if (name == null) {
      jsonGenerator.writeBoolean(object);
    } else {
      jsonGenerator.writeBooleanField(name, object);
    }
  }

  private void writeDate(String name, Date object) throws IOException {
    if (name == null) {
      jsonGenerator.writeNumber(object.getTime());
    } else {
      jsonGenerator.writeNumberField(name, object.getTime());
    }
  }

  private void writeNumber(String name, Number object) throws IOException {

    if (object instanceof Long) {
      writeLong(name, object);

    } else if (object instanceof Integer) {
      writeInteger(name, object);

    } else if (object instanceof Double) {
      writeDouble(name, object);

    } else if (object instanceof BigDecimal) {
      writeBigDecimal(name, object);

    } else if (object instanceof BigInteger) {
      writeBigInteger(name, object);

    } else {
      writeGeneralNumber(name, object);
    }
  }

  private void writeGeneralNumber(String name, Number object) throws IOException {
    writeBigDecimal(name, new BigDecimal(object.toString()));
  }

  private void writeBigDecimal(String name, Number object) throws IOException {
    if (name == null) {
      jsonGenerator.writeNumber((BigDecimal) object);
    } else {
      jsonGenerator.writeNumberField(name, (BigDecimal) object);
    }
  }

  private void writeBigInteger(String name, Number object) throws IOException {
    if (name == null) {
      jsonGenerator.writeNumber((BigInteger) object);
    } else {
      jsonGenerator.writeNumberField(name, object.longValue());
    }
  }

  private void writeDouble(String name, Number object) throws IOException {
    if (name == null) {
      jsonGenerator.writeNumber((Double) object);
    } else {
      jsonGenerator.writeNumberField(name, (Double) object);
    }
  }

  private void writeLong(String name, Number object) throws IOException {
    if (name == null) {
      jsonGenerator.writeNumber((Long) object);
    } else {
      jsonGenerator.writeNumberField(name, (Long) object);
    }
  }

  private void writeInteger(String name, Number object) throws IOException {
    if (name == null) {
      jsonGenerator.writeNumber((Integer) object);
    } else {
      jsonGenerator.writeNumberField(name, (Integer) object);
    }
  }

  private void writeNull(String name) throws IOException {
    if (name == null) {
      jsonGenerator.writeNull();
    } else {
      jsonGenerator.writeNullField(name);
    }
  }

  private void writeString(String name, String object) throws IOException {
    if (name == null) {
      jsonGenerator.writeString(object);
    } else {
      jsonGenerator.writeStringField(name, object);
    }
  }

  private void writeCollection(String name, Collection<Object> collection) throws IOException {
    if (name != null) {
      jsonGenerator.writeFieldName(name);
    }
    jsonGenerator.writeStartArray();
    for (Object object : collection) {
      writeJson(null, object);
    }
    jsonGenerator.writeEndArray();
  }

  private void writeMap(String name, Map<Object, Object> map) throws IOException {

    if (name != null) {
      jsonGenerator.writeFieldName(name);
    }
    jsonGenerator.writeStartObject();
    Set<Entry<Object, Object>> entrySet = map.entrySet();
    for (Entry<Object, Object> entry : entrySet) {
      writeJson(entry.getKey().toString(), entry.getValue());
    }
    jsonGenerator.writeEndObject();
  }

}
