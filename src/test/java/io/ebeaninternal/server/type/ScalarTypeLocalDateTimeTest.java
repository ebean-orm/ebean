package io.ebeaninternal.server.type;

import io.ebean.config.JsonConfig;
import org.junit.Test;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import static org.junit.Assert.*;

public class ScalarTypeLocalDateTimeTest {

  ScalarTypeLocalDateTime type = new ScalarTypeLocalDateTime(JsonConfig.DateTime.MILLIS);

  // warm up
  LocalDateTime warmUp = LocalDateTime.now();

  @Test
  public void testNowToMillis() throws Exception {

    warmUp.hashCode();

    long now = System.currentTimeMillis();
    long toMillis = type.convertToMillis(LocalDateTime.now());
    assertTrue(toMillis - now < 30);
  }

  @Test
  public void testConvertToMillis() throws Exception {

    LocalDateTime now = LocalDateTime.now();
    long asMillis = type.convertToMillis(now);
    LocalDateTime fromMillis = type.convertFromMillis(asMillis);

    assertEquals(now, fromMillis);
  }

  @Test
  public void testConvertFromTimestamp() throws Exception {

    Timestamp now = new Timestamp(System.currentTimeMillis());

    LocalDateTime localDateTime = type.convertFromTimestamp(now);
    Timestamp timestamp = type.convertToTimestamp(localDateTime);

    assertEquals(now, timestamp);
  }


  @Test
  public void testToJdbcType() throws Exception {

    Object jdbcType = type.toJdbcType(LocalDateTime.now());
    assertTrue(jdbcType instanceof Timestamp);

    jdbcType = type.toJdbcType(new Timestamp(System.currentTimeMillis()));
    assertTrue(jdbcType instanceof Timestamp);
  }

  @Test
  public void testToBeanType() throws Exception {

    Timestamp timestamp = new Timestamp(System.currentTimeMillis());
    LocalDateTime val1 = type.toBeanType(timestamp);
    assertNotNull(val1);

    LocalDateTime val2 = type.toBeanType(timestamp.toLocalDateTime());
    assertNotNull(val2);

    Timestamp timestamp1 = type.convertToTimestamp(val1);
    assertEquals(timestamp, timestamp1);
  }

  @Test
  public void testJson() throws Exception {

    LocalDateTime now = LocalDateTime.now();

    JsonTester<LocalDateTime> jsonTester = new JsonTester<>(type);
    jsonTester.test(now);

    ScalarTypeLocalDateTime typeNanos = new ScalarTypeLocalDateTime(JsonConfig.DateTime.NANOS);
    jsonTester = new JsonTester<>(typeNanos);
    jsonTester.test(now);

    ScalarTypeLocalDateTime typeIso = new ScalarTypeLocalDateTime(JsonConfig.DateTime.ISO8601);
    jsonTester = new JsonTester<>(typeIso);
    jsonTester.test(now);
  }
}
