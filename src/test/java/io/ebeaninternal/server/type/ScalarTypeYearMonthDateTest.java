package io.ebeaninternal.server.type;

import org.junit.Test;

import java.sql.Date;
import java.time.LocalDate;
import java.time.YearMonth;

import static org.junit.Assert.assertEquals;

public class ScalarTypeYearMonthDateTest {

  ScalarTypeYearMonthDate type = new ScalarTypeYearMonthDate();

  @Test
  public void testConvertFromMillis() throws Exception {

    LocalDate today = LocalDate.now();
    LocalDate firstMonthDay = today.withDayOfMonth(1);
    Date date = Date.valueOf(firstMonthDay);
    long epochMilli = date.getTime();

    YearMonth yearMonth = type.convertFromMillis(epochMilli);
    long val1 = type.convertToMillis(yearMonth);

    assertEquals(epochMilli, val1);
  }

  @Test
  public void testConvertDate() throws Exception {

    LocalDate today = LocalDate.now();
    LocalDate firstMonthDay = today.withDayOfMonth(1);
    Date date = Date.valueOf(firstMonthDay);

    YearMonth yearMonth = type.convertFromDate(date);
    Date date1 = type.convertToDate(yearMonth);
    assertEquals(date, date1);
  }


  @Test
  public void testToJdbcType() throws Exception {

    LocalDate today = LocalDate.now();
    LocalDate firstMonthDay = today.withDayOfMonth(1);
    Date date = Date.valueOf(firstMonthDay);

    YearMonth yearMonth = type.toBeanType(date);
    Object val1 = type.toJdbcType(yearMonth);
    assertEquals(date, val1);
  }

}
