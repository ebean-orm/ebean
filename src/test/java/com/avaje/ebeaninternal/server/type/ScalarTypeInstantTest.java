package com.avaje.ebeaninternal.server.type;

import com.avaje.ebean.config.JsonConfig;
import com.avaje.ebean.text.TextException;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import org.junit.Test;

import java.io.*;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.Year;

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
    assertEquals(timestamp.toString(), formatted);
  }

  @Test
  public void testParse() throws Exception {

    Instant now = Instant.now();
    Timestamp timestamp = Timestamp.from(now);
    Instant val1 = type.parse(timestamp.toString());
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

    StringWriter writer = new StringWriter();
    JsonFactory factory = new JsonFactory();
    JsonGenerator generator = factory.createGenerator(writer);
    generator.writeStartObject();
    type.jsonWrite(generator, "key", now);
    generator.writeEndObject();
    generator.flush();

    JsonParser parser = factory.createParser(writer.toString());
    JsonToken token = parser.nextToken();
    assertEquals(JsonToken.START_OBJECT, token);
    token = parser.nextToken();
    assertEquals(JsonToken.FIELD_NAME, token);
    token = parser.nextToken();

    Instant val1 = type.jsonRead(parser, token);
    assertEquals(now, val1);

  }

}