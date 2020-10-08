package io.ebeaninternal.server.type;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import io.ebeaninternal.json.ModifyAwareMap;
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
    Map<String, Object> emptyMap = new HashMap<>();
    assertTrue(hstore.isDirty(emptyMap));

    ModifyAwareMap<String, Object> modAware = new ModifyAwareMap<>(emptyMap);
    assertFalse(hstore.isDirty(modAware));
    modAware.put("foo", "Rob");
    assertTrue(hstore.isDirty(emptyMap));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testParse() throws Exception {
    Map<String, Object> map = (Map<String, Object>) hstore.parse("{\"name\":\"rob\"}");
    assertEquals(1, map.size());
    assertEquals("rob", map.get("name"));
  }

  @Test(expected = RuntimeException.class)
  @SuppressWarnings("unchecked")
  public void testParseDateTime() throws Exception {
    Map<String, Object> map = (Map<String, Object>) hstore.convertFromMillis(1234L);
    assertEquals(1, map.size());
    assertEquals("rob", map.get("name"));
  }

  @Test
  public void testJsonWrite() throws Exception {

    Map<String, Object> map = new LinkedHashMap<>();

    assertEquals("{\"key\":{}}", generateJson(map));

    map.put("name", "rob");
    assertEquals("{\"key\":{\"name\":\"rob\"}}", generateJson(map));

    map.put("age", 12);
    assertEquals("{\"key\":{\"name\":\"rob\",\"age\":12}}", generateJson(map));
  }

  @Test
  public void testJsonRead() throws Exception {

    Map<String, Object> map = parseHstore("{\"name\":\"rob\"}");
    assertEquals(1, map.size());
    assertEquals("rob", map.get("name"));

    map = parseHstore("{\"name\":\"rob\",\"age\":12}");
    assertEquals(2, map.size());
    assertEquals("rob", map.get("name"));
    assertEquals(12L, map.get("age"));

  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> parseHstore(String json) throws IOException {
    JsonParser parser = jsonFactory.createParser(json);
    // BeanProperty reads the first token checking for null so
    // simulate that here
    JsonToken token = parser.nextToken();
    assertEquals(JsonToken.START_OBJECT, token);
    return (Map<String, Object>) hstore.jsonRead(parser);
  }

  private String generateJson(Map<String, Object> map) throws IOException {

    StringWriter writer = new StringWriter();
    JsonGenerator generator = jsonFactory.createGenerator(writer);
    // wrap in an object to form proper json
    generator.writeStartObject();
    generator.writeFieldName("key");

    hstore.jsonWrite(generator, map);

    generator.writeEndObject();
    generator.flush();

    return writer.toString();
  }

}
