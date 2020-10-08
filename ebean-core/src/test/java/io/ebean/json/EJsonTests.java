package io.ebean.json;

import io.ebean.text.json.EJson;
import io.ebeaninternal.json.ModifyAwareMap;
import io.ebeaninternal.json.ModifyAwareOwner;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

public class EJsonTests {

  private static final Logger log = LoggerFactory.getLogger(EJsonTests.class);

  @Test
  public void test_map_simple() throws IOException {

    JsonFactory factory = new JsonFactory();

    String jsonInput = "{\"name\":\"rob\",\"age\":12}";

    JsonParser jsonParser = factory.createParser(jsonInput);

    Object result = EJson.parse(jsonParser);

    assertTrue(result instanceof Map);
    Map<?, ?> map = (Map<?, ?>) result;
    assertEquals("rob", map.get("name"));
    assertEquals(12L, map.get("age"));

    String jsonOutput = EJson.write(result);
    assertEquals(jsonInput, jsonOutput);
  }

  @Test
  public void write_withWriter_expect_writerNotClosed() throws IOException {

    File temp = Files.createTempFile("some", ".json").toFile();
    FileWriter writer = new FileWriter(temp);
    Map<String,Object> map = new LinkedHashMap<>();
    map.put("foo", "bar");
    EJson.write(map, writer);
    writer.write("The end.");
    writer.flush();
    writer.close();

    log.info("write to file {}", temp.getAbsolutePath());
  }

  @Test
  public void test_parseObject() throws IOException {

    JsonFactory factory = new JsonFactory();

    String jsonInput = "{\"name\":\"rob\",\"age\":12}";

    JsonParser jsonParser = factory.createParser(jsonInput);

    Map<String, Object> map = EJson.parseObject(jsonParser);

    assertNotNull(map);
    assertEquals("rob", map.get("name"));
    assertEquals(12L, map.get("age"));

    String jsonOutput = EJson.write(map);
    assertEquals(jsonInput, jsonOutput);
  }

  @Test
  public void test_parseObject_reader() throws IOException {

    String jsonInput = "{\"name\":\"rob\",\"age\":12}";

    StringReader reader = new StringReader(jsonInput);

    Map<String, Object> map = EJson.parseObject(reader);

    assertNotNull(map);
    assertEquals("rob", map.get("name"));
    assertEquals(12L, map.get("age"));

    String jsonOutput = EJson.write(map);
    assertEquals(jsonInput, jsonOutput);
  }

  @Test
  public void test_map_nested() throws IOException {

    String jsonInput = "{\"name\":\"rob\",\"age\":12,\"org\":{\"name\":\"superorg\",\"rating\":4},\"nums\":[1,2,3]}";
    Object result = EJson.parse(jsonInput);

    assertTrue(result instanceof Map);
    Map<?, ?> map = (Map<?, ?>) result;
    assertEquals(4, map.size());
    assertEquals("rob", map.get("name"));
    assertEquals(12L, map.get("age"));

    Map<?, ?> org = (Map<?, ?>) map.get("org");
    assertEquals("superorg", org.get("name"));
    assertEquals(4L, org.get("rating"));

    List<?> nums = (List<?>) map.get("nums");
    assertEquals(3, nums.size());
    assertEquals(1L, nums.get(0));
    assertEquals(2L, nums.get(1));
    assertEquals(3L, nums.get(2));

    String jsonOutput = EJson.write(result);
    assertEquals(jsonInput, jsonOutput);
  }

  @Test
  public void test_map_withNull() throws IOException {

    String jsonInput = "{\"name\":\"rob\",\"age\":null}";
    Object result = EJson.parse(jsonInput);

    assertTrue(result instanceof Map);
    Map<?, ?> map = (Map<?, ?>) result;
    assertEquals("rob", map.get("name"));
    assertNull(map.get("age"));

    String jsonOutput = EJson.write(result);
    assertEquals(jsonInput, jsonOutput);
  }

  @Test
  public void test_list_simple() throws IOException {

    String jsonInput = "[\"name\",\"rob\",12,13]";
    List<Object> list = EJson.parseList(jsonInput);

    assertEquals(4, list.size());
    assertEquals("name", list.get(0));
    assertEquals("rob", list.get(1));
    assertEquals(12L, list.get(2));
    assertEquals(13L, list.get(3));

    String jsonOutput = EJson.write(list);
    assertEquals(jsonInput, jsonOutput);
  }

  @Test
  public void test_list_reader() throws IOException {

    String jsonInput = "[\"name\",\"rob\",12,13]";
    StringReader reader = new StringReader(jsonInput);
    List<Object> list = EJson.parseList(reader);

    assertEquals(4, list.size());
    assertEquals("name", list.get(0));
    assertEquals("rob", list.get(1));
    assertEquals(12L, list.get(2));
    assertEquals(13L, list.get(3));

    String jsonOutput = EJson.write(list);
    assertEquals(jsonInput, jsonOutput);
  }

  @Test
  public void test_list_jsonParser() throws IOException {

    String jsonInput = "[\"name\",\"rob\",12,13]";

    JsonFactory jsonFactory = new JsonFactory();
    JsonParser parser = jsonFactory.createParser(jsonInput);

    List<Object> list = EJson.parseList(parser);

    assertEquals(4, list.size());
    assertEquals("name", list.get(0));
    assertEquals("rob", list.get(1));
    assertEquals(12L, list.get(2));
    assertEquals(13L, list.get(3));

    String jsonOutput = EJson.write(list);
    assertEquals(jsonInput, jsonOutput);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void test_list_ofMaps() throws IOException {

    String jsonInput = "[{\"name\":\"rob\",\"age\":12},{\"name\":\"mike\",\"age\":13}]";
    Object result = EJson.parse(jsonInput);

    assertTrue(result instanceof List);

    List<Map<?, ?>> list = (List<Map<?, ?>>) result;
    assertEquals(2, list.size());
    assertEquals("rob", list.get(0).get("name"));
    assertEquals(12L, list.get(0).get("age"));
    assertEquals("mike", list.get(1).get("name"));
    assertEquals(13L, list.get(1).get("age"));

    String jsonOutput = EJson.write(result);
    assertEquals(jsonInput, jsonOutput);
  }

  @Test
  public void test_partial_read() throws IOException {

    String jsonInput = "{\"name\":\"rob\",\"age\":null,\"friend\":{\"name\":\"mike\",\"age\":13}},some more json would follow...";
    StringReader reader = new StringReader(jsonInput);

    Object result = EJson.parse(reader);

    assertTrue(result instanceof Map);
    Map<?, ?> map = (Map<?, ?>) result;
    assertEquals("rob", map.get("name"));
    assertNull(map.get("age"));

    Map<?, ?> friend = (Map<?, ?>) map.get("friend");
    assertEquals("mike", friend.get("name"));
    assertEquals(13L, friend.get("age"));
  }


  @Test
  public void test_map_nested_modifyAware() throws IOException {

    String jsonInput = "{\"name\":\"rob\",\"age\":12,\"org\":{\"name\":\"superorg\",\"rating\":4},\"nums\":[1,2,3]}";
    ModifyAwareMap<String, Object> map = (ModifyAwareMap<String, Object>) EJson.parseObject(jsonInput, true);

    assertFalse(map.isMarkedDirty());
    assertEquals(4, map.size());

    map.put("name", "jim");
    assertTrue(map.isMarkedDirty());
  }

  @SuppressWarnings("unchecked")
  @Test
  public void test_map_nested_modifyAwareNestedList() throws IOException {

    String jsonInput = "{\"name\":\"rob\",\"age\":12,\"org\":{\"name\":\"superorg\",\"rating\":4},\"nums\":[1,2,3]}";
    ModifyAwareMap<String, Object> map = (ModifyAwareMap<String, Object>) EJson.parseObject(jsonInput, true);
    assertFalse(map.isMarkedDirty());

    List<Object> nums = (List<Object>) map.get("nums");
    nums.add(4);
    assertTrue(map.isMarkedDirty());
  }

  @SuppressWarnings("unchecked")
  @Test
  public void test_map_nested_modifyAwareNestedObject() throws IOException {

    String jsonInput = "{\"name\":\"rob\",\"age\":12,\"org\":{\"name\":\"superorg\",\"rating\":4},\"nums\":[1,2,3]}";
    ModifyAwareMap<String, Object> map = (ModifyAwareMap<String, Object>) EJson.parseObject(jsonInput, true);
    assertFalse(map.isMarkedDirty());

    Map<String, Object> org = (Map<String, Object>) map.get("org");
    org.put("extra", "foo");
    assertTrue(map.isMarkedDirty());
  }


  @Test
  public void parse_when_null() throws IOException {

    Object nothing = EJson.parse((String) null);
    assertNull(nothing);
  }

  @Test
  public void parseList_when_null() throws IOException {

    Object nothing = EJson.parseList((String) null);
    assertNull(nothing);
  }

  @Test
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public void parseSet_when_modifyAware() throws IOException {

    String jsonInput = "[{\"name\":\"rob\",\"age\":12},{\"name\":\"jim\",\"age\":42}]";

    Set set = EJson.parseSet(jsonInput, true);

    ModifyAwareOwner modAware = (ModifyAwareOwner) set;
    assertFalse(modAware.isMarkedDirty());

    Iterator iterator = set.iterator();
    if (iterator.hasNext()) {
      Map map = (Map) iterator.next();
      map.put("name", "stu");
      assertTrue(modAware.isMarkedDirty());
    }
  }

  @Test
  public void parseSet_when_not_modifyAware() throws IOException {

    String jsonInput = "[{\"name\":\"rob\",\"age\":12},{\"name\":\"jim\",\"age\":42}]";

    Set<?> set = EJson.parseSet(jsonInput, false);
    assertTrue(set instanceof LinkedHashSet);
  }
}
