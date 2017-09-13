package io.ebeaninternal.server.type;

import io.ebean.config.JsonConfig;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Timestamp;
import java.time.Instant;

import static org.junit.Assert.*;

public class ScalarTypeInstantTest {

  ScalarTypeInstant type = new ScalarTypeInstant(JsonConfig.DateTime.MILLIS);

  @Test
  public void testReadData() throws Exception {

    ByteArrayOutputStream os = new ByteArrayOutputStream();
    ObjectOutputStream out = new ObjectOutputStream(os);

    Instant now = Instant.now();
    type.writeData(out, now);
    type.writeData(out, null);
    out.flush();
    out.close();

    ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
    ObjectInputStream in = new ObjectInputStream(is);

    Instant val1 = type.readData(in);
    Instant val2 = type.readData(in);

    assertEquals(now, val1);
    assertNull(val2);
  }

  @Test
  public void testToJdbcType() throws Exception {

    Instant now = Instant.now();
    Timestamp timestamp = Timestamp.from(now);
    Object val1 = type.toJdbcType(now);
    Object val2 = type.toJdbcType(timestamp);

    assertEquals(timestamp, val1);
    assertEquals(timestamp, val2);
  }

  @Test
  public void testToBeanType() throws Exception {

    Instant now = Instant.now();
    Instant val1 = type.toBeanType(now);
    Instant val2 = type.toBeanType(Timestamp.from(now));

    assertEquals(now, val1);
    assertEquals(now, val2);
  }

  @Test
  public void testFormatValue() throws Exception {

    Instant now = Instant.now();
    Timestamp timestamp = Timestamp.from(now);
    String formatted = type.formatValue(now);
    assertEquals("" + timestamp.getTime(), formatted);
  }

  @Test
  public void testParse_when_epochMillis() throws Exception {

    Instant now = Instant.now();
    Timestamp timestamp = Timestamp.from(now);
    Instant val1 = type.parse("" + timestamp.getTime());
    assertEquals(now, val1);
  }

  @Test
  public void testParse_when_timestampForm() throws Exception {

    Instant now = Instant.now();
    Timestamp timestamp = Timestamp.from(now);
    Instant val1 = type.parse(timestamp.toString());
    assertEquals(now, val1);
  }

  @Test
  public void testFormatAndParse() throws Exception {

    Instant now = Instant.now();

    String format = type.format(now);
    Instant val1 = type.parse(format);
    assertEquals(now, val1);
  }

  @Test
  public void testIsDateTimeCapable() throws Exception {

    assertTrue(type.isDateTimeCapable());
  }

  @Test
  public void testConvertFromMillis() throws Exception {

    Instant now = Instant.now();
    Instant val = type.convertFromMillis(now.toEpochMilli());
    assertEquals(now, val);
  }

  @Test
  public void testJsonRead() throws Exception {


    Instant now = Instant.now();

    JsonTester<Instant> jsonTester = new JsonTester<>(type);
    jsonTester.test(now);

    ScalarTypeInstant typeNanos = new ScalarTypeInstant(JsonConfig.DateTime.NANOS);
    jsonTester = new JsonTester<>(typeNanos);
    jsonTester.test(now);

    ScalarTypeInstant typeIso = new ScalarTypeInstant(JsonConfig.DateTime.ISO8601);
    jsonTester = new JsonTester<>(typeIso);
    jsonTester.test(now);

  }

}
