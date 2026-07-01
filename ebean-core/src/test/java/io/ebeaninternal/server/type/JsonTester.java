package io.ebeaninternal.server.type;

import io.avaje.json.JsonReader;
import io.avaje.json.JsonWriter;
import io.avaje.json.stream.JsonStream;
import io.ebean.config.JsonConfig;
import io.ebean.core.type.ScalarType;
import io.ebeaninternal.server.json.WriteJson;

import java.io.IOException;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Base class to help json testing.
 */
public class JsonTester<T> {

  protected JsonStream jsonStream = JsonStream.builder().build();

  protected ScalarType<T> type;

  public JsonTester(ScalarType<T> type) {
    this.type = type;
  }

  public String test(T value) throws IOException {
    StringWriter writer = new StringWriter();

    JsonWriter generator = jsonStream.writer(writer);
    generator.beginObject();
    WriteJson writeJson = new WriteJson(generator, JsonConfig.Include.ALL);
    writeJson.writeFieldName("key");
    type.jsonWrite(generator, value);
    generator.endObject();
    generator.flush();

    JsonReader parser = jsonStream.reader(writer.toString());
    parser.beginObject();
    assertTrue(parser.hasNextField());
    assertEquals("key", parser.nextField());
    T val1 = type.jsonRead(parser);
    assertEquals(value, val1);
    parser.endObject();

    return writer.toString();
  }
}
