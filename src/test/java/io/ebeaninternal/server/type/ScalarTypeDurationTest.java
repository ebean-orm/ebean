package io.ebeaninternal.server.type;

import io.ebean.text.TextException;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.Duration;

import static org.junit.Assert.*;

public class ScalarTypeDurationTest {

  ScalarTypeDuration type = new ScalarTypeDuration();

  @Test
  public void testReadData() throws Exception {

    Duration duration = Duration.ofSeconds(1234);

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

    Duration duration = Duration.ofSeconds(1234);
    long seconds = duration.getSeconds();

    Object val1 = type.toJdbcType(duration);
    Object val2 = type.toJdbcType(seconds);

    assertEquals(seconds, val1);
    assertEquals(seconds, val2);
  }

  @Test
  public void testToBeanType() throws Exception {

    Duration duration = Duration.ofSeconds(1234);
    long seconds = duration.getSeconds();

    Duration val1 = type.toBeanType(duration);
    Duration val2 = type.toBeanType(seconds);
    Duration val3 = type.toBeanType((int) seconds);

    assertEquals(duration, val1);
    assertEquals(duration, val2);
    assertEquals(duration, val3);
  }

  @Test
  public void testFormatValue() throws Exception {

    Duration duration = Duration.ofSeconds(1234);
    String formatValue = type.formatValue(duration);
    assertEquals("PT20M34S", formatValue);
  }

  @Test
  public void testParse() throws Exception {

    Duration duration = type.parse("PT20M34S");
    assertEquals(Duration.ofSeconds(1234), duration);
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

    Duration duration = Duration.ofSeconds(1234);

    JsonTester<Duration> jsonTester = new JsonTester<>(type);
    jsonTester.test(duration);

  }

}
