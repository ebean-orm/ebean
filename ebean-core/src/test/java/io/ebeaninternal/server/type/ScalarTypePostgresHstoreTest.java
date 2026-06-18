package io.ebeaninternal.server.type;

import io.avaje.json.JsonReader;
import io.avaje.json.JsonReader.Token;
import io.avaje.json.JsonWriter;
import io.avaje.json.stream.JsonStream;
import io.ebeaninternal.json.ModifyAwareMap;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class ScalarTypePostgresHstoreTest {

  ScalarTypePostgresHstore hstore = new ScalarTypePostgresHstore();

  JsonStream jsonStream = JsonStream.builder().build();

  @Test
  public void testIsMutable() {
    assertTrue(hstore.mutable());
  }

  @Test
  public void testIsDirty() {
    Map<String, Object> emptyMap = new HashMap<>();
    assertTrue(hstore.isDirty(emptyMap));

    ModifyAwareMap<String, Object> modAware = new ModifyAwareMap<>(emptyMap);
    assertFalse(hstore.isDirty(modAware));
    modAware.put("foo", "Rob");
    assertTrue(hstore.isDirty(emptyMap));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testParse() {
    Map<String, Object> map = (Map<String, Object>) hstore.parse("{\"name\":\"rob\"}");
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
    JsonReader parser = jsonStream.reader(json);
    // BeanProperty reads the first token checking for null so
    // simulate that here
    Token token = parser.currentToken();
    assertEquals(Token.BEGIN_OBJECT, token);
    return (Map<String, Object>) hstore.jsonRead(parser);
  }

  private String generateJson(Map<String, Object> map) throws IOException {

    StringWriter writer = new StringWriter();
    JsonWriter generator = jsonStream.writer(writer);
    // wrap in an object to form proper json
    generator.beginObject();
    generator.name("key");

    hstore.jsonWrite(generator, map);

    generator.endObject();
    generator.flush();

    return writer.toString();
  }

}
