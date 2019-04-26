package io.ebeaninternal.server.type;

import io.ebean.config.JsonConfig;
import org.junit.Test;

import java.io.IOException;
import java.sql.Date;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

public class ScalarTypeYearMonthDateTest {

  private ScalarTypeYearMonthDate type = new ScalarTypeYearMonthDate(JsonConfig.Date.MILLIS);

  @Test
  public void testConvertFromMillis() {

    LocalDate today = LocalDate.now();
    LocalDate firstMonthDay = today.withDayOfMonth(1);
    ZonedDateTime zonedDateTime = firstMonthDay.atStartOfDay(ZoneOffset.UTC);

    long epochMilli = zonedDateTime.toInstant().toEpochMilli();

    YearMonth yearMonth = type.convertFromMillis(epochMilli);
    long val1 = type.convertToMillis(yearMonth);

    assertEquals(epochMilli, val1);
  }

  @Test
  public void testConvertDate() {

    LocalDate today = LocalDate.now();
    LocalDate firstMonthDay = today.withDayOfMonth(1);
    Date date = Date.valueOf(firstMonthDay);

    YearMonth yearMonth = type.convertFromDate(date);
    Date date1 = type.convertToDate(yearMonth);
    assertEquals(date, date1);
  }


  @Test
  public void testToJdbcType() {

    LocalDate today = LocalDate.now();
    LocalDate firstMonthDay = today.withDayOfMonth(1);
    Date date = Date.valueOf(firstMonthDay);

    YearMonth yearMonth = type.toBeanType(date);
    Object val1 = type.toJdbcType(yearMonth);
    assertEquals(date, val1);
  }

  @Test
  public void json() throws IOException {

    YearMonth val = YearMonth.of(2019, 5);

    JsonTester<YearMonth> jsonMillis = new JsonTester<>(type);
    assertThat(jsonMillis.test(val)).isEqualTo("{\"key\":1556668800000}");

    JsonTester<YearMonth> jsonIso = new JsonTester<>(new ScalarTypeYearMonthDate(JsonConfig.Date.ISO8601) );
    assertThat(jsonIso.test(val)).isEqualTo("{\"key\":\"2019-05-01\"}");
  }

}
