package io.ebeaninternal.server.type;

import org.junit.jupiter.api.Test;

import java.sql.Date;
import java.time.LocalDate;
import java.time.MonthDay;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ScalarTypeMonthDayTest {

  ScalarTypeMonthDay type = new ScalarTypeMonthDay();

  @Test
  public void testToJdbcType() throws Exception {

    MonthDay value = MonthDay.of(4, 29);
    Date date = Date.valueOf(LocalDate.of(2000, 4, 29));

    Object val1 = type.toJdbcType(value);
    Object val2 = type.toJdbcType(date);

    assertEquals(date, val1);
    assertEquals(date, val2);
  }

  @Test
  public void testToBeanType() throws Exception {

    MonthDay value = MonthDay.of(4, 29);
    Date date = Date.valueOf(LocalDate.of(2000, 4, 29));

    MonthDay val1 = type.toBeanType(value);
    MonthDay val2 = type.toBeanType(date);

    assertEquals(value, val1);
    assertEquals(value, val2);
  }

  @Test
  public void testFormatParse() {
    MonthDay value = MonthDay.of(4, 29);
    String val1 = type.formatValue(value);
    MonthDay monthDay = type.parse(val1);

    assertEquals("--04-29", val1);
    assertEquals(value, monthDay);
  }

  @Test
  public void testConvertFromMillis() {
    assertThrows(RuntimeException.class, () -> type.convertFromMillis(1203));
  }

  @Test
  public void testJson() throws Exception {
    MonthDay value = MonthDay.of(4, 29);
    JsonTester<MonthDay> jsonTester = new JsonTester<>(type);
    jsonTester.test(value);
  }

}
