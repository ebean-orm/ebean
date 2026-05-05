package io.ebeaninternal.json;


import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

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
  public void testToString() {

    ModifyAwareMap<String, String> map = createMap();
    assertEquals(map.map.toString(), map.toString());
  }

  @Test
  public void testIsMarkedDirty() {

    ModifyAwareMap<String, String> map = createMap();
    assertFalse(map.isMarkedDirty());

    map.put("A", "change");
    assertTrue(map.isMarkedDirty());
  }

  @Test
  public void testMarkAsModified() {

    ModifyAwareMap<String, String> map = createMap();
    assertFalse(map.isMarkedDirty());

    map.setMarkedDirty(true);
    assertTrue(map.isMarkedDirty());
  }

  @Test
  public void testSize() {

    ModifyAwareMap<String, String> map = createMap();
    assertEquals(5, map.size());
  }

  @Test
  public void testIsEmpty() {

    assertFalse(createMap().isEmpty());
    assertTrue(createEmptyMap().isEmpty());
  }

  @Test
  public void testContainsKey() {

    ModifyAwareMap<String, String> map = createMap();
    assertTrue(map.containsKey("A"));
    assertFalse(map.containsKey("Z"));
  }

  @Test
  public void testContainsValue() {
    ModifyAwareMap<String, String> map = createMap();
    assertTrue(map.containsValue("one"));
    assertFalse(map.containsValue("junk"));
  }

  @Test
  public void testGet() {

    ModifyAwareMap<String, String> map = createMap();

    assertEquals("two", map.get("B"));
    assertNull(map.get("Z"));
    assertFalse(map.isMarkedDirty());
  }

  @Test
  public void testPut() {

    ModifyAwareMap<String, String> map = createMap();
    assertFalse(map.isMarkedDirty());

    map.put("A", "mod");
    assertTrue(map.isMarkedDirty());
  }

  @Test
  public void testRemove() {

    ModifyAwareMap<String, String> map = createMap();
    assertFalse(map.isMarkedDirty());

    map.remove("A");
    assertTrue(map.isMarkedDirty());
  }

  @Test
  public void testPutAllWithEmpty() {

    ModifyAwareMap<String, String> map = createMap();
    assertFalse(map.isMarkedDirty());

    Map<String, String> other = new HashMap<>();
    map.putAll(other);
    assertTrue(map.isMarkedDirty());
  }

  @Test
  public void testPutAll() {

    ModifyAwareMap<String, String> map = createMap();
    assertFalse(map.isMarkedDirty());

    Map<String, String> other = new HashMap<>();
    other.put("A", "one");
    map.putAll(other);
    assertTrue(map.isMarkedDirty());
  }

  @Test
  public void testClear() {

    ModifyAwareMap<String, String> map = createMap();
    assertFalse(map.isMarkedDirty());

    map.clear();
    assertTrue(map.isMarkedDirty());
  }

  @Test
  public void testKeySet() {

    ModifyAwareMap<String, String> map = createMap();
    assertFalse(map.isMarkedDirty());

    Set<String> keys = map.keySet();
    assertEquals(map.size(), keys.size());
    assertTrue(keys.contains("A"));
    assertFalse(map.isMarkedDirty());
  }

  @Test
  public void testValues() {

    ModifyAwareMap<String, String> map = createMap();
    assertFalse(map.isMarkedDirty());

    Collection<String> values = map.values();
    assertEquals(map.size(), values.size());
    assertTrue(values.contains("one"));
    assertFalse(map.isMarkedDirty());
  }

  @Test
  public void testEntrySet() {

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

    @SuppressWarnings("unchecked")
    ModifyAwareMap<String, String> read = (ModifyAwareMap<String, String>) ois.readObject();
    assertThat(read).hasSize(orig.size());
  }

  @Test
  public void equalsWhenEqual() {

    ModifyAwareMap<String, String> mapA = createMap();
    ModifyAwareMap<String, String> mapB = createMap();

    assertThat(mapA).isEqualTo(mapB);
    assertThat(mapA.hashCode()).isEqualTo(mapB.hashCode());
  }

  @Test
  public void equalsWhenNotEqual() {

    ModifyAwareMap<String, String> mapA = createMap();
    ModifyAwareMap<String, String> mapB = createMap();
    mapB.put("F", "Six");

    assertThat(mapA).isNotEqualTo(mapB);
    assertThat(mapA.hashCode()).isNotEqualTo(mapB.hashCode());
  }
}
