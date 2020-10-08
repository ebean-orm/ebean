package io.ebeaninternal.server.type;

import io.ebean.text.TextException;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.Year;

import static org.junit.Assert.*;

public class ScalarTypeYearTest {

  ScalarTypeYear type = new ScalarTypeYear();

  @Test
  public void testReadData() throws Exception {

    ByteArrayOutputStream os = new ByteArrayOutputStream();
    ObjectOutputStream out = new ObjectOutputStream(os);

    type.writeData(out, Year.of(2013));
    type.writeData(out, null);
    out.flush();
    out.close();

    ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
    ObjectInputStream in = new ObjectInputStream(is);

    Year year1 = type.readData(in);
    Year year2 = type.readData(in);

    assertEquals(Year.of(2013), year1);
    assertNull(year2);
  }

  @Test
  public void testToJdbcType() throws Exception {

    Integer year = 2013;
    Object val1 = type.toJdbcType(Year.of(2013));
    Object val2 = type.toJdbcType(2013);
    Object val3 = type.toJdbcType(2013L);

    assertEquals(year, val1);
    assertEquals(year, val2);
    assertEquals(year, val3);
  }

  @Test
  public void testToBeanType() throws Exception {

    Year year = Year.of(2013);
    Year val1 = type.toBeanType(year);
    Year val2 = type.toBeanType(2013);
    Year val3 = type.toBeanType(2013L);

    assertEquals(year, val1);
    assertEquals(year, val2);
    assertEquals(year, val3);
  }

  @Test
  public void testFormatValue() throws Exception {

    String formatted = type.formatValue(Year.of(2013));
    assertEquals("2013", formatted);
  }

  @Test
  public void testParse() throws Exception {

    Year year = type.parse("2013");
    assertEquals(Year.of(2013), year);
  }

  @Test
  public void testIsDateTimeCapable() throws Exception {

    assertFalse(type.isDateTimeCapable());
  }

  @Test(expected = TextException.class)
  public void testConvertFromMillis() throws Exception {

    type.convertFromMillis(1000);
  }

  @Test
  public void testJson() throws Exception {

    JsonTester<Year> jsonTester = new JsonTester<>(type);
    jsonTester.test(Year.of(2013));
  }

}
