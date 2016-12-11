package io.ebeaninternal.server.type;

import org.junit.Test;

import java.sql.Date;
import java.time.LocalDate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ScalarTypeLocalDateTest {

  ScalarTypeLocalDate type = new ScalarTypeLocalDate();

  @Test
  public void testConvertToMillis() throws Exception {

    LocalDate date = LocalDate.of(2014, 5, 20);
    long millis = type.convertToMillis(date);

    LocalDate parseDate = type.convertFromMillis(millis);
    assertEquals(date, parseDate);
  }

  @Test
  public void testConvertFromDate() throws Exception {

    LocalDate localDate = LocalDate.now();
    Date date = Date.valueOf(localDate);

    LocalDate localDate1 = type.convertFromDate(date);
    assertEquals(localDate, localDate1);

    Date date1 = type.convertToDate(localDate);
    assertEquals(date, date1);
  }


  @Test
  public void testToJdbcType() throws Exception {

    LocalDate localDate = LocalDate.now();
    Object o = type.toJdbcType(localDate);
    assertTrue(o instanceof Date);
  }

  @Test
  public void testToBeanType() throws Exception {

    LocalDate localDate = LocalDate.now();
    Date date = Date.valueOf(localDate);

    LocalDate localDate1 = type.toBeanType(date);
    assertEquals(localDate, localDate1);
  }

}
