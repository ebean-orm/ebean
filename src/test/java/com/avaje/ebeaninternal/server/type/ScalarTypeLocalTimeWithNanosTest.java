package com.avaje.ebeaninternal.server.type;

import com.avaje.ebean.text.TextException;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import org.junit.Test;

import java.io.*;
import java.time.LocalTime;

import static org.junit.Assert.*;

public class ScalarTypeLocalTimeWithNanosTest {

  ScalarTypeLocalTimeWithNanos type = new ScalarTypeLocalTimeWithNanos();

  @Test
  public void testReadData() throws Exception {

    ByteArrayOutputStream os = new ByteArrayOutputStream();
    ObjectOutputStream out = new ObjectOutputStream(os);

    LocalTime localTime = LocalTime.of(9, 23, 45, 115);
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

    LocalTime localTime = LocalTime.of(9, 23, 45, 115);
    long asNanos = localTime.toNanoOfDay();
    Object val1 = type.toJdbcType(localTime);
    Object val2 = type.toJdbcType(asNanos);

    assertEquals(asNanos, val1);
    assertEquals(asNanos, val2);
  }

  @Test
  public void testToBeanType() throws Exception {

    LocalTime localTime = LocalTime.of(9, 23, 45);
    LocalTime val1 = type.toBeanType(localTime);
    LocalTime val2 = type.toBeanType(localTime.toNanoOfDay());

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


    StringWriter writer = new StringWriter();
    JsonFactory factory = new JsonFactory();
    JsonGenerator generator = factory.createGenerator(writer);
    generator.writeStartObject();
    type.jsonWrite(generator, "key", localTime);
    generator.writeEndObject();
    generator.flush();

    JsonParser parser = factory.createParser(writer.toString());
    JsonToken token = parser.nextToken();
    assertEquals(JsonToken.START_OBJECT, token);
    token = parser.nextToken();
    assertEquals(JsonToken.FIELD_NAME, token);
    token = parser.nextToken();

    LocalTime val1 = type.jsonRead(parser, token);
    assertEquals(localTime, val1);

  }

}