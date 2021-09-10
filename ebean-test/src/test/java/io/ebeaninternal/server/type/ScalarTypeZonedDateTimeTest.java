package io.ebeaninternal.server.type;

import io.ebean.config.JsonConfig;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.TimeZone;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class ScalarTypeZonedDateTimeTest {


  ScalarTypeZonedDateTime type = new ScalarTypeZonedDateTime(JsonConfig.DateTime.MILLIS, ZoneId.systemDefault());

  ZonedDateTime warmUp = ZonedDateTime.now();

  @Test
  public void testConvertToMillis() {

    warmUp.hashCode();

    long now = System.currentTimeMillis();
    long toMillis = type.convertToMillis(ZonedDateTime.now());

    assertTrue(toMillis - now < 10);

  }

  @Test
  public void testConvertFromTimestamp() {

    Timestamp now = new Timestamp(System.currentTimeMillis());

    ZonedDateTime val1 = type.convertFromTimestamp(now);
    Timestamp timestamp = type.convertToTimestamp(val1);

    assertEquals(now, timestamp);
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
      ScalarTypeZonedDateTime type = new ScalarTypeZonedDateTime(JsonConfig.DateTime.MILLIS, timeZoneToUse.toZoneId());

      // effectively we desire to ignore the system timezone and use the configured one
      TimeZone.setDefault(timeZoneToUse);

      final ZonedDateTime zonedDateTime = type.convertFromInstant(dateTime.toInstant());

      assertEquals(expectedZoneOffset, zonedDateTime.getOffset());

    } finally {
      TimeZone.setDefault(previous);
    }
  }

  @Test
  public void testToJdbcType() throws Exception {

    Object jdbcType = type.toJdbcType(ZonedDateTime.now());
    assertTrue(jdbcType instanceof Timestamp);
  }

  @Test
  public void testToBeanType() throws Exception {

    Timestamp timestamp = new Timestamp(System.currentTimeMillis());
    ZonedDateTime localDateTime = type.toBeanType(timestamp);

    assertNotNull(localDateTime);

    Timestamp timestamp1 = type.convertToTimestamp(localDateTime);
    assertEquals(timestamp, timestamp1);

  }

  @Test
  public void testJson() throws Exception {

    ZonedDateTime now = ZonedDateTime.now().withNano(123_000_000); // jdk11 workaround

    JsonTester<ZonedDateTime> jsonTester = new JsonTester<>(type);
    jsonTester.test(now);

    ScalarTypeZonedDateTime typeNanos = new ScalarTypeZonedDateTime(JsonConfig.DateTime.NANOS, ZoneId.systemDefault());
    jsonTester = new JsonTester<>(typeNanos);
    jsonTester.test(now);

    ScalarTypeZonedDateTime typeIso = new ScalarTypeZonedDateTime(JsonConfig.DateTime.ISO8601, ZoneId.systemDefault());
    jsonTester = new JsonTester<>(typeIso);
    jsonTester.test(now);
  }

  @Test
  public void toJsonISO8601() {

    ScalarTypeZonedDateTime typeIso = new ScalarTypeZonedDateTime(JsonConfig.DateTime.ISO8601, ZoneId.systemDefault());

    ZonedDateTime now = ZonedDateTime.now();
    String asJson = typeIso.toJsonISO8601(now);

    ZonedDateTime value = typeIso.fromJsonISO8601(asJson);
    assertThat(now).isEqualTo(value);
  }
}
