package io.ebean.joda.time;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import io.ebean.config.JsonConfig;
import org.joda.time.DateMidnight;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ScalarTypeJodaDateMidnightTest {

  ScalarTypeJodaDateMidnight type = new ScalarTypeJodaDateMidnight(JsonConfig.Date.ISO8601);
  JsonFactory jsonFactory = new JsonFactory();

  @Test
  void testIsoFormat() throws IOException {
    // long millis = 1674472451640L;
    JsonParser parser = jsonFactory.createParser(new StringReader("\"2023-01-23\""));
    JsonToken token = parser.nextToken();
    assertEquals(JsonToken.VALUE_STRING, token);
    DateMidnight dm = type.jsonRead(parser);
    String iso = type.toIsoFormat(dm);
    assertEquals("2023-01-23", iso);
  }
}
