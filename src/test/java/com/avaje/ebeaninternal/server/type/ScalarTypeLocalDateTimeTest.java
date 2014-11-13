package com.avaje.ebeaninternal.server.type;

import org.junit.Test;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import static org.junit.Assert.*;

public class ScalarTypeLocalDateTimeTest {

  ScalarTypeLocalDateTime type = new ScalarTypeLocalDateTime();

  // warm up
  LocalDateTime warmUp = LocalDateTime.now();

  @Test
  public void testConvertToMillis() throws Exception {

    warmUp.hashCode();

    long now = System.currentTimeMillis();
    long toMillis = type.convertToMillis( LocalDateTime.now());

    assertTrue(toMillis - now < 10);
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
  }

  @Test
  public void testToBeanType() throws Exception {

    Timestamp timestamp = new Timestamp(System.currentTimeMillis());
    LocalDateTime localDateTime = type.toBeanType(timestamp);

    assertNotNull(localDateTime);
    
    Timestamp timestamp1 = type.convertToTimestamp(localDateTime);
    assertEquals(timestamp, timestamp1);
  }
}