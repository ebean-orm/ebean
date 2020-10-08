package io.ebeaninternal.server.type;

import io.ebean.text.TextException;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.time.Duration;

import static org.junit.Assert.*;

public class ScalarTypeDurationWithNanosTest {

  ScalarTypeDurationWithNanos type = new ScalarTypeDurationWithNanos();

  @Test
  public void testReadData() throws Exception {

    Duration duration = Duration.ofSeconds(323, 1500000);

    ByteArrayOutputStream os = new ByteArrayOutputStream();
    ObjectOutputStream out = new ObjectOutputStream(os);

    type.writeData(out, duration);
    type.writeData(out, null);
    out.flush();
    out.close();

    ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
    ObjectInputStream in = new ObjectInputStream(is);

    Duration val1 = type.readData(in);
    Duration val2 = type.readData(in);

    assertEquals(duration, val1);
    assertNull(val2);
  }

  @Test
  public void testToJdbcType() throws Exception {

    Duration duration = Duration.ofSeconds(323, 1500000);
    BigDecimal bigDecimal = DecimalUtils.toDecimal(duration);

    Object val1 = type.toJdbcType(duration);
    Object val2 = type.toJdbcType(bigDecimal);

    assertEquals(bigDecimal, val1);
    assertEquals(bigDecimal, val2);
  }

  @Test
  public void testToBeanType() throws Exception {

    Duration duration = Duration.ofSeconds(323, 1500000);
    BigDecimal bigDecimal = DecimalUtils.toDecimal(duration);

    Duration val1 = type.toBeanType(duration);
    Duration val2 = type.toBeanType(bigDecimal);

    assertEquals(duration, val1);
    assertEquals(duration, val2);
  }

  @Test
  public void testFormatValue() throws Exception {

    Duration duration = Duration.ofSeconds(323, 1500000);
    String formatValue = type.formatValue(duration);
    assertEquals("PT5M23.0015S", formatValue);
  }

  @Test
  public void testParse() throws Exception {

    Duration duration = Duration.ofSeconds(323, 1500000);
    Duration val1 = type.parse("PT5M23.0015S");
    assertEquals(duration, val1);
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
  public void testJsonRead() throws Exception {

    Duration duration = Duration.ofSeconds(323, 1500000);

    JsonTester<Duration> jsonTester = new JsonTester<>(type);
    jsonTester.test(duration);
  }

}
