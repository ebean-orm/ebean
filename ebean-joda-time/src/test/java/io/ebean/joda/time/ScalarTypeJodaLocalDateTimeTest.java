package io.ebean.joda.time;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import io.ebean.config.JsonConfig;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringReader;
import java.sql.Timestamp;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ScalarTypeJodaLocalDateTimeTest {

  ScalarTypeJodaLocalDateTime type = new ScalarTypeJodaLocalDateTime(JsonConfig.DateTime.ISO8601);
  JsonFactory jsonFactory = new JsonFactory();

  @Test
  void testIsoFormat() throws IOException {
    JsonParser parser = jsonFactory.createParser(new StringReader("\"2023-01-23T12:14:11.640\""));
    JsonToken token = parser.nextToken();
    assertEquals(JsonToken.VALUE_STRING, token);

    LocalDateTime ldt1 = type.jsonRead(parser);
    String iso = type.toJsonISO8601(ldt1);
    assertEquals("2023-01-23T12:14:11.640", iso);
  }

  @Test
  void testConvertFromTimestamp() {
    long now = System.currentTimeMillis();
    Timestamp nowTs = new Timestamp(now);

    LocalDateTime ldt1 = type.convertFromTimestamp(nowTs);
    LocalDateTime ldt2 = localConvertFromTimestamp(nowTs);

    assertEquals(ldt1, ldt2);

    Timestamp ts1 = type.convertToTimestamp(ldt1);
    Timestamp ts2 = localConvertToTimestamp(ldt2);

    assertEquals(ts1, ts2);
  }

  LocalDateTime localConvertFromTimestamp(Timestamp ts) {
    return new LocalDateTime(ts.getTime(), DateTimeZone.getDefault());
  }

  Timestamp localConvertToTimestamp(LocalDateTime t) {
    return new Timestamp(t.toDateTime(DateTimeZone.getDefault()).getMillis());
  }

}
