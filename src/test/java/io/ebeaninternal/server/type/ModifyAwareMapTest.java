package io.ebeaninternal.server.type;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

public class ModifyAwareMapTest {

  private ModifyAwareMap<String, String> createMap() {
    LinkedHashMap<String, String> map = new LinkedHashMap<>();
    map.put("A", "one");
    map.put("B", "two");
    map.put("C", "three");
    map.put("D", "four");
    map.put("E", "five");
    return new ModifyAwareMap<>(map);
  }

  private ModifyAwareMap<String, String> createEmptyMap() {
    LinkedHashMap<String, String> map = new LinkedHashMap<>();
    return new ModifyAwareMap<>(map);
  }

  @Test
  public void testToString() throws Exception {

    ModifyAwareMap<String, String> map = createMap();
    assertEquals(map.map.toString(), map.toString());
  }

  @Test
  public void testIsMarkedDirty() throws Exception {

    ModifyAwareMap<String, String> map = createMap();
    assertFalse(map.isMarkedDirty());

    map.put("A", "change");
    assertTrue(map.isMarkedDirty());
  }

  @Test
  public void testMarkAsModified() throws Exception {

    ModifyAwareMap<String, String> map = createMap();
    assertFalse(map.isMarkedDirty());

    map.markAsModified();
    assertTrue(map.isMarkedDirty());
  }

  @Test
  public void testSize() throws Exception {

    ModifyAwareMap<String, String> map = createMap();
    assertEquals(5, map.size());
  }

  @Test
  public void testIsEmpty() throws Exception {

    assertFalse(createMap().isEmpty());
    assertTrue(createEmptyMap().isEmpty());
  }

  @Test
  public void testContainsKey() throws Exception {

    ModifyAwareMap<String, String> map = createMap();
    assertTrue(map.containsKey("A"));
    assertFalse(map.containsKey("Z"));
  }

  @Test
  public void testContainsValue() throws Exception {
    ModifyAwareMap<String, String> map = createMap();
    assertTrue(map.containsValue("one"));
    assertFalse(map.containsValue("junk"));
  }

  @Test
  public void testGet() throws Exception {

    ModifyAwareMap<String, String> map = createMap();

    assertEquals("two", map.get("B"));
    assertNull(map.get("Z"));
    assertFalse(map.isMarkedDirty());
  }

  @Test
  public void testPut() throws Exception {

    ModifyAwareMap<String, String> map = createMap();
    assertFalse(map.isMarkedDirty());

    map.put("A", "mod");
    assertTrue(map.isMarkedDirty());
  }

  @Test
  public void testRemove() throws Exception {

    ModifyAwareMap<String, String> map = createMap();
    assertFalse(map.isMarkedDirty());

    map.remove("A");
    assertTrue(map.isMarkedDirty());
  }

  @Test
  public void testPutAllWithEmpty() throws Exception {

    ModifyAwareMap<String, String> map = createMap();
    assertFalse(map.isMarkedDirty());

    Map<String, String> other = new HashMap<>();
    map.putAll(other);
    assertTrue(map.isMarkedDirty());
  }

  @Test
  public void testPutAll() throws Exception {

    ModifyAwareMap<String, String> map = createMap();
    assertFalse(map.isMarkedDirty());

    Map<String, String> other = new HashMap<>();
    other.put("A", "one");
    map.putAll(other);
    assertTrue(map.isMarkedDirty());
  }

  @Test
  public void testClear() throws Exception {

    ModifyAwareMap<String, String> map = createMap();
    assertFalse(map.isMarkedDirty());

    map.clear();
    assertTrue(map.isMarkedDirty());
  }

  @Test
  public void testKeySet() throws Exception {

    ModifyAwareMap<String, String> map = createMap();
    assertFalse(map.isMarkedDirty());

    Set<String> keys = map.keySet();
    assertEquals(map.size(), keys.size());
    assertTrue(keys.contains("A"));
    assertFalse(map.isMarkedDirty());
  }

  @Test
  public void testValues() throws Exception {

    ModifyAwareMap<String, String> map = createMap();
    assertFalse(map.isMarkedDirty());

    Collection<String> values = map.values();
    assertEquals(map.size(), values.size());
    assertTrue(values.contains("one"));
    assertFalse(map.isMarkedDirty());
  }

  @Test
  public void testEntrySet() throws Exception {

    ModifyAwareMap<String, String> map = createMap();
    Set<Map.Entry<String, String>> entries = map.entrySet();

    assertFalse(map.isMarkedDirty());

    assertEquals(map.size(), entries.size());
    assertFalse(map.isMarkedDirty());
  }

  @Test
  public void serialise() throws IOException, ClassNotFoundException {

    ByteArrayOutputStream os = new ByteArrayOutputStream();
    ObjectOutputStream oos = new ObjectOutputStream(os);

    ModifyAwareMap<String, String> orig = createMap();
    oos.writeObject(orig);
    oos.flush();
    oos.close();

    ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
    ObjectInputStream ois = new ObjectInputStream(is);

    ModifyAwareMap<String, String> read = (ModifyAwareMap<String, String>)ois.readObject();
    assertThat(read).hasSize(orig.size());
  }
}
