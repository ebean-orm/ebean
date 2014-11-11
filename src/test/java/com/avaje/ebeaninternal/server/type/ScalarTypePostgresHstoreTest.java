package com.avaje.ebeaninternal.server.type;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class ScalarTypePostgresHstoreTest {

  ScalarTypePostgresHstore hstore = new ScalarTypePostgresHstore();

  JsonFactory jsonFactory = new JsonFactory();

  @Test
  public void testIsMutable() throws Exception {
    assertTrue(hstore.isMutable());
  }

  @Test
  public void testIsDateTimeCapable() throws Exception {
    assertFalse(hstore.isDateTimeCapable());
  }

  @Test
  public void testIsDirty() throws Exception {
    Map<String,Object> emptyMap = new HashMap<String, Object>();
    assertTrue(hstore.isDirty(emptyMap));

    ModifyAwareMap<String,Object> modAware = new ModifyAwareMap<String,Object>(emptyMap);
    assertFalse(hstore.isDirty(modAware));
    modAware.put("foo", "Rob");
    assertTrue(hstore.isDirty(emptyMap));
  }

  @Test
  public void testParse() throws Exception {
    Map<String,Object> map = (Map<String,Object>)hstore.parse("{\"name\":\"rob\"}");
    assertEquals(1, map.size());
    assertEquals("rob", map.get("name"));
  }

  @Test(expected = RuntimeException.class)
  public void testParseDateTime() throws Exception {
    Map<String,Object> map = (Map<String,Object>)hstore.parseDateTime(1234L);
    assertEquals(1, map.size());
    assertEquals("rob", map.get("name"));
  }

  @Test
  public void testJsonWrite() throws Exception {
    Map<String,Object> map = new LinkedHashMap<String, Object>();

    assertEquals("{}", generateJson(map));

    map.put("name", "rob");
    assertEquals("{\"name\":\"rob\"}", generateJson(map));

    map.put("age", 12);
    assertEquals("{\"name\":\"rob\",\"age\":12}", generateJson(map));
  }

  @Test
  public void testJsonRead() throws Exception {

    Map<String, Object> map = parse("{\"name\":\"rob\"}");
    assertEquals(1, map.size());
    assertEquals("rob", map.get("name"));

    map = parse("{\"name\":\"rob\",\"age\":12}");
    assertEquals(2, map.size());
    assertEquals("rob", map.get("name"));
    assertEquals(12L, map.get("age"));

  }

  private Map<String,Object> parse(String json) throws IOException {
    JsonParser parser = jsonFactory.createParser(json);
    return (Map<String,Object>)hstore.jsonRead(parser, JsonToken.FIELD_NAME);
  }

  private String generateJson(Map<String, Object> emptyMap) throws IOException {
    StringWriter writer = new StringWriter();
    JsonGenerator generator = jsonFactory.createGenerator(writer);
    hstore.jsonWrite(generator, "name", emptyMap);
    generator.flush();
    return writer.toString();
  }

}