package com.avaje.ebeaninternal.server.type;

import org.junit.Test;

import java.time.LocalDate;

import static org.junit.Assert.*;

public class ScalarTypeLocalDateTest {

  ScalarTypeLocalDate type = new ScalarTypeLocalDate();

  @Test
  public void testConvertToMillis() throws Exception {

    LocalDate date = LocalDate.of(2014, 5, 20);
    long millis = type.convertToMillis(date);

    System.out.println(date);

    LocalDate parseDate = type.parseDateTime(millis);

    assertEquals(date, parseDate);
  }

  @Test
  public void testConvertFromDate() throws Exception {

  }

  @Test
  public void testConvertToDate() throws Exception {

  }

  @Test
  public void testToJdbcType() throws Exception {

  }

  @Test
  public void testToBeanType() throws Exception {

  }

  @Test
  public void testParseDateTime() throws Exception {

  }
}