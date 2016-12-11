package io.ebeaninternal.server.type;

import org.junit.Test;

import java.sql.Date;
import java.time.LocalDate;
import java.time.MonthDay;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

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
  public void testFormatParse() throws Exception {

    MonthDay value = MonthDay.of(4, 29);
    String val1 = type.formatValue(value);
    MonthDay monthDay = type.parse(val1);

    assertEquals("--04-29", val1);
    assertEquals(value, monthDay);
  }


  @Test
  public void testIsDateTimeCapable() throws Exception {
    assertFalse(type.isDateTimeCapable());
  }

  @Test(expected = RuntimeException.class)
  public void testConvertFromMillis() throws Exception {
    type.convertFromMillis(1203);
  }

  @Test
  public void testJson() throws Exception {

    MonthDay value = MonthDay.of(4, 29);
    JsonTester<MonthDay> jsonTester = new JsonTester<>(type);
    jsonTester.test(value);
  }

  @Test
  public void testReadWriteData() throws Exception {

  }
}
