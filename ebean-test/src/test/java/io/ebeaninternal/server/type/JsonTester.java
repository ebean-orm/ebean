package io.ebeaninternal.server.type;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import io.ebean.config.JsonConfig;
import io.ebean.core.type.ScalarType;
import io.ebeaninternal.server.text.json.WriteJson;

import java.io.IOException;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Base class to help json testing.
 */
public class JsonTester<T> {

  protected JsonFactory factory = new JsonFactory();

  protected ScalarType<T> type;

  public JsonTester(ScalarType<T> type) {
    this.type = type;
  }

  public String test(T value) throws IOException {
    StringWriter writer = new StringWriter();

    JsonGenerator generator = factory.createGenerator(writer);
    generator.writeStartObject();

    WriteJson writeJson = new WriteJson(generator, JsonConfig.Include.ALL);
    writeJson.writeFieldName("key");
    type.jsonWrite(generator, value);
    generator.writeEndObject();
    generator.flush();

    JsonParser parser = factory.createParser(writer.toString());
    JsonToken token = parser.nextToken();
    assertEquals(JsonToken.START_OBJECT, token);
    token = parser.nextToken();
    assertEquals(JsonToken.FIELD_NAME, token);
    parser.nextToken();

    T val1 = type.jsonRead(parser);
    assertEquals(value, val1);

    return writer.toString();
  }
}
