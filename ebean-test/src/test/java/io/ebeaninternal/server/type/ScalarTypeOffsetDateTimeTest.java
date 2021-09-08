package io.ebeaninternal.server.type;

import io.ebean.config.JsonConfig;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.TimeZone;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ScalarTypeOffsetDateTimeTest {


  ScalarTypeOffsetDateTime type = new ScalarTypeOffsetDateTime(JsonConfig.DateTime.MILLIS, ZoneOffset.systemDefault());

  OffsetDateTime warmUp = OffsetDateTime.now();

  @Test
  public void testConvertToMillis() {

    warmUp.hashCode();

    long now = System.currentTimeMillis();
    long toMillis = type.convertToMillis(OffsetDateTime.now());

    assertTrue(toMillis - now < 10);
  }

  @Test
  public void convertFromInstant_with_UTC_expect_matchingZoneOffset() {
    final TimeZone timeZoneToUse = TimeZone.getTimeZone("UTC");
    final ZoneOffset expectedZoneOffset = ZoneOffset.UTC;

    convertFromInstantWithConfiguredTimeZone(timeZoneToUse, expectedZoneOffset);
  }

  @Test
  public void convertFromInstant_with_EST_expect_matchingZoneOffset() {
    final TimeZone timeZoneToUse = TimeZone.getTimeZone("EST");
    final ZoneOffset expectedOffset = OffsetDateTime.now(timeZoneToUse.toZoneId()).getOffset();

    convertFromInstantWithConfiguredTimeZone(timeZoneToUse, expectedOffset);
  }

  private void convertFromInstantWithConfiguredTimeZone(TimeZone timeZoneToUse, ZoneOffset expectedZoneOffset) {
    TimeZone previous = TimeZone.getDefault();
    try {
      OffsetDateTime dateTime = OffsetDateTime.parse("2021-01-01T00:00:00+11:00");

      // test ScalarTypeOffsetDateTime with the configured timeZone to use
      ScalarTypeOffsetDateTime type = new ScalarTypeOffsetDateTime(JsonConfig.DateTime.MILLIS, timeZoneToUse.toZoneId());

      // effectively we desire to ignore the system timezone and use the configured one
      TimeZone.setDefault(timeZoneToUse);

      final OffsetDateTime offsetDateTime = type.convertFromInstant(dateTime.toInstant());

      assertEquals(expectedZoneOffset, offsetDateTime.getOffset());

    } finally {
      TimeZone.setDefault(previous);
    }
  }

  @Test
  public void testConvertFromTimestamp() {

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

    ScalarTypeOffsetDateTime typeNanos = new ScalarTypeOffsetDateTime(JsonConfig.DateTime.NANOS, ZoneOffset.systemDefault());
    jsonTester = new JsonTester<>(typeNanos);
    jsonTester.test(now);

    ScalarTypeOffsetDateTime typeIso = new ScalarTypeOffsetDateTime(JsonConfig.DateTime.ISO8601, ZoneOffset.systemDefault());
    jsonTester = new JsonTester<>(typeIso);
    jsonTester.test(now);
  }

  @Test
  public void isoJsonFormatParse() {

    ScalarTypeOffsetDateTime typeIso = new ScalarTypeOffsetDateTime(JsonConfig.DateTime.ISO8601, ZoneOffset.systemDefault());

    OffsetDateTime now = OffsetDateTime.now();
    String asJson = typeIso.toJsonISO8601(now);

    OffsetDateTime value = typeIso.fromJsonISO8601(asJson);
    assertThat(now).isEqualToIgnoringNanos(value);
  }
}
