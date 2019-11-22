package io.ebeaninternal.server.type;

import io.ebean.config.JsonConfig;
import org.junit.Test;

import java.sql.Timestamp;
import java.time.OffsetDateTime;

import static org.assertj.core.api.StrictAssertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ScalarTypeOffsetDateTimeTest {


  ScalarTypeOffsetDateTime type = new ScalarTypeOffsetDateTime(JsonConfig.DateTime.MILLIS);

  OffsetDateTime warmUp = OffsetDateTime.now();

  @Test
  public void testConvertToMillis() throws Exception {

    warmUp.hashCode();

    long now = System.currentTimeMillis();
    long toMillis = type.convertToMillis(OffsetDateTime.now());

    assertTrue(toMillis - now < 10);
  }

  @Test
  public void testConvertFromTimestamp() throws Exception {

    Timestamp now = new Timestamp(System.currentTimeMillis());

    OffsetDateTime localDateTime = type.convertFromTimestamp(now);
    Timestamp timestamp = type.convertToTimestamp(localDateTime);

    assertEquals(now, timestamp);
  }


  @Test
  public void testToJdbcType() throws Exception {

    Object jdbcType = type.toJdbcType(OffsetDateTime.now());
    assertTrue(jdbcType instanceof Timestamp);
  }

  @Test
  public void testToBeanType() throws Exception {

    Timestamp timestamp = new Timestamp(System.currentTimeMillis());
    OffsetDateTime localDateTime = type.toBeanType(timestamp);

    assertNotNull(localDateTime);

    Timestamp timestamp1 = type.convertToTimestamp(localDateTime);
    assertEquals(timestamp, timestamp1);

  }

  @Test
  public void testJson() throws Exception {

    OffsetDateTime now = OffsetDateTime.now().withNano(123_000_000); // jdk11 workaround

    JsonTester<OffsetDateTime> jsonTester = new JsonTester<>(type);
    jsonTester.test(now);

    ScalarTypeOffsetDateTime typeNanos = new ScalarTypeOffsetDateTime(JsonConfig.DateTime.NANOS);
    jsonTester = new JsonTester<>(typeNanos);
    jsonTester.test(now);

    ScalarTypeOffsetDateTime typeIso = new ScalarTypeOffsetDateTime(JsonConfig.DateTime.ISO8601);
    jsonTester = new JsonTester<>(typeIso);
    jsonTester.test(now);
  }

  @Test
  public void isoJsonFormatParse() {

    ScalarTypeOffsetDateTime typeIso = new ScalarTypeOffsetDateTime(JsonConfig.DateTime.ISO8601);

    OffsetDateTime now = OffsetDateTime.now();
    String asJson = typeIso.toJsonISO8601(now);

    OffsetDateTime value = typeIso.fromJsonISO8601(asJson);
    assertThat(now).isEqualToIgnoringNanos(value);
  }
}
