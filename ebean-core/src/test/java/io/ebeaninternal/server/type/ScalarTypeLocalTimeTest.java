package io.ebeaninternal.server.type;

import io.ebean.text.TextException;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Time;
import java.time.LocalTime;

import static org.junit.Assert.*;

public class ScalarTypeLocalTimeTest {

  ScalarTypeLocalTime type = new ScalarTypeLocalTime();

  @Test
  public void testReadData() throws Exception {

    ByteArrayOutputStream os = new ByteArrayOutputStream();
    ObjectOutputStream out = new ObjectOutputStream(os);

    LocalTime localTime = LocalTime.of(9, 23, 45);
    type.writeData(out, localTime);
    type.writeData(out, null);
    out.flush();
    out.close();

    ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
    ObjectInputStream in = new ObjectInputStream(is);

    LocalTime val1 = type.readData(in);
    LocalTime val2 = type.readData(in);

    assertEquals(localTime, val1);
    assertNull(val2);
  }

  @Test
  public void testToJdbcType() throws Exception {

    LocalTime localTime = LocalTime.of(9, 23, 45, 123);
    Time time = Time.valueOf(localTime);

    Object val1 = type.toJdbcType(localTime);
    Object val2 = type.toJdbcType(time);

    assertEquals(time, val1);
    assertEquals(time, val2);
  }

  @Test
  public void testToBeanType() throws Exception {

    LocalTime localTime = LocalTime.of(9, 23, 45);
    LocalTime val1 = type.toBeanType(localTime);
    LocalTime val2 = type.toBeanType(Time.valueOf(localTime));

    assertEquals(localTime, val1);
    assertEquals(localTime, val2);
  }

  @Test
  public void testFormatValue() throws Exception {

    LocalTime localTime = LocalTime.of(9, 23, 45);
    String formatted = type.formatValue(localTime);
    assertEquals("09:23:45", formatted);
  }

  @Test
  public void testParse() throws Exception {

    LocalTime localTime = LocalTime.of(9, 23, 45);
    LocalTime val1 = type.parse("09:23:45");
    assertEquals(localTime, val1);
  }

  @Test
  public void testIsDateTimeCapable() throws Exception {

    assertFalse(type.isDateTimeCapable());
  }

  @Test(expected = TextException.class)
  public void testConvertFromMillis() throws Exception {

    type.convertFromMillis(1234);
  }

  @Test
  public void testJsonRead() throws Exception {

    LocalTime localTime = LocalTime.of(9, 23, 45);

    JsonTester<LocalTime> jsonTester = new JsonTester<>(type);
    jsonTester.test(localTime);
  }

}
