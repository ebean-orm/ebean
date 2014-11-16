package com.avaje.ebeaninternal.server.type;

import com.avaje.ebean.config.JsonConfig;
import org.junit.Test;

import java.sql.Timestamp;
import java.time.OffsetDateTime;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

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
}