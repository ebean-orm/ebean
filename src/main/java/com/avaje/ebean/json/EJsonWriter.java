package com.avaje.ebean.json;

import java.io.StringWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.json.Json;
import javax.json.stream.JsonGenerator;

class EJsonWriter {

  public static String write(Object object) {
    StringWriter writer = new StringWriter(200);
    write(object, writer);
    return writer.toString();
  }

  public static void write(Object object, Writer writer) {
    JsonGenerator generator = Json.createGenerator(writer);
    write(object, generator);
    generator.close();
  }

  public static void write(Object object, JsonGenerator jsonGenerator) {
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

    } else {
      writeString(name, object.toString());
    }

  }

  private void writeBoolean(String name, Boolean object) {
    if (name == null) {
      jsonGenerator.write(object);
    } else {
      jsonGenerator.write(name, object);
    }
  }

  private void writeDate(String name, Date object) {
    if (name == null) {
      jsonGenerator.write(object.getTime());
    } else {
      jsonGenerator.write(name, object.getTime());
    }
  }

  private void writeNumber(String name, Number object) {

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

  private void writeGeneralNumber(String name, Number object) {
    if (name == null) {
      jsonGenerator.write(new BigDecimal(object.toString()));
    } else {
      jsonGenerator.write(name, new BigDecimal(object.toString()));
    }
  }

  private void writeBigDecimal(String name, Number object) {
    if (name == null) {
      jsonGenerator.write((BigDecimal) object);
    } else {
      jsonGenerator.write(name, (BigDecimal) object);
    }
  }

  private void writeBigInteger(String name, Number object) {
    if (name == null) {
      jsonGenerator.write((BigInteger) object);
    } else {
      jsonGenerator.write(name, (BigInteger) object);
    }
  }

  private void writeDouble(String name, Number object) {
    if (name == null) {
      jsonGenerator.write((Double) object);
    } else {
      jsonGenerator.write(name, (Double) object);
    }
  }

  private void writeLong(String name, Number object) {
    if (name == null) {
      jsonGenerator.write((Long) object);
    } else {
      jsonGenerator.write(name, (Long) object);
    }
  }

  private void writeInteger(String name, Number object) {
    if (name == null) {
      jsonGenerator.write((Integer) object);
    } else {
      jsonGenerator.write(name, (Integer) object);
    }
  }

  private void writeNull(String name) {
    if (name == null) {
      jsonGenerator.writeNull();
    } else {
      jsonGenerator.writeNull(name);
    }
  }

  private void writeString(String name, String object) {
    if (name == null) {
      jsonGenerator.write(object);
    } else {
      jsonGenerator.write(name, object);
    }
  }

  private void writeCollection(String name, Collection<Object> collection) {
    if (name == null) {
      jsonGenerator.writeStartObject();
    } else {
      jsonGenerator.writeStartArray(name);
    }
    for (Object object : collection) {
      writeJson(null, object);
    }
    jsonGenerator.writeEnd();
  }

  private void writeMap(String name, Map<Object, Object> map) {

    if (name == null) {
      jsonGenerator.writeStartObject();
    } else {
      jsonGenerator.writeStartObject(name);
    }
    Set<Entry<Object, Object>> entrySet = map.entrySet();
    for (Entry<Object, Object> entry : entrySet) {
      writeJson(entry.getKey().toString(), entry.getValue());
    }
    jsonGenerator.writeEnd();
  }

}
