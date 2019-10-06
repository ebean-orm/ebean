package io.ebeaninternal.server.type;

import io.ebean.config.JsonConfig;
import org.junit.Test;

import java.io.IOException;
import java.sql.Date;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ScalarTypeLocalDateTest {

  ScalarTypeLocalDate type = new ScalarTypeLocalDate(JsonConfig.Date.ISO8601);

  @Test
  public void testConvertToMillis() {

    LocalDate date = LocalDate.of(2014, 5, 20);
    long millis = type.convertToMillis(date);

    LocalDate parseDate = type.convertFromMillis(millis);
    assertEquals(date, parseDate);
  }

  @Test
  public void testConvertFromDate() {

    LocalDate localDate = LocalDate.now();
    Date date = Date.valueOf(localDate);

    LocalDate localDate1 = type.convertFromDate(date);
    assertEquals(localDate, localDate1);

    Date date1 = type.convertToDate(localDate);
    assertEquals(date, date1);
  }


  @Test
  public void testToJdbcType() {

    LocalDate localDate = LocalDate.now();
    Object o = type.toJdbcType(localDate);
    assertTrue(o instanceof Date);
  }

  @Test
  public void testToBeanType() {

    LocalDate localDate = LocalDate.now();
    Date date = Date.valueOf(localDate);

    LocalDate localDate1 = type.toBeanType(date);
    assertEquals(localDate, localDate1);
  }

  @Test
  public void json() throws IOException {

    LocalDate val = LocalDate.of(2019, 5, 9);

    JsonTester<LocalDate> jsonMillis = new JsonTester<>(new ScalarTypeLocalDate(JsonConfig.Date.MILLIS));
    assertThat(jsonMillis.test(val)).isEqualTo("{\"key\":1557360000000}");

    JsonTester<LocalDate> jsonIso = new JsonTester<>(new ScalarTypeLocalDate(JsonConfig.Date.ISO8601) );
    assertThat(jsonIso.test(val)).isEqualTo("{\"key\":\"2019-05-09\"}");
  }
}
