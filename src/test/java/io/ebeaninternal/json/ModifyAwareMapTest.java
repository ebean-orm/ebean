package io.ebeaninternal.json;


import org.assertj.core.api.Assertions;
import org.junit.Assert;
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
    Assert.assertEquals(map.map.toString(), map.toString());
  }

  @Test
  public void testIsMarkedDirty() throws Exception {

    ModifyAwareMap<String, String> map = createMap();
    Assert.assertFalse(map.isMarkedDirty());

    map.put("A", "change");
    Assert.assertTrue(map.isMarkedDirty());
  }

  @Test
  public void testMarkAsModified() throws Exception {

    ModifyAwareMap<String, String> map = createMap();
    Assert.assertFalse(map.isMarkedDirty());

    map.markAsModified();
    Assert.assertTrue(map.isMarkedDirty());
  }

  @Test
  public void testSize() throws Exception {

    ModifyAwareMap<String, String> map = createMap();
    Assert.assertEquals(5, map.size());
  }

  @Test
  public void testIsEmpty() throws Exception {

    Assert.assertFalse(createMap().isEmpty());
    Assert.assertTrue(createEmptyMap().isEmpty());
  }

  @Test
  public void testContainsKey() throws Exception {

    ModifyAwareMap<String, String> map = createMap();
    Assert.assertTrue(map.containsKey("A"));
    Assert.assertFalse(map.containsKey("Z"));
  }

  @Test
  public void testContainsValue() throws Exception {
    ModifyAwareMap<String, String> map = createMap();
    Assert.assertTrue(map.containsValue("one"));
    Assert.assertFalse(map.containsValue("junk"));
  }

  @Test
  public void testGet() throws Exception {

    ModifyAwareMap<String, String> map = createMap();

    Assert.assertEquals("two", map.get("B"));
    Assert.assertNull(map.get("Z"));
    Assert.assertFalse(map.isMarkedDirty());
  }

  @Test
  public void testPut() throws Exception {

    ModifyAwareMap<String, String> map = createMap();
    Assert.assertFalse(map.isMarkedDirty());

    map.put("A", "mod");
    Assert.assertTrue(map.isMarkedDirty());
  }

  @Test
  public void testRemove() throws Exception {

    ModifyAwareMap<String, String> map = createMap();
    Assert.assertFalse(map.isMarkedDirty());

    map.remove("A");
    Assert.assertTrue(map.isMarkedDirty());
  }

  @Test
  public void testPutAllWithEmpty() throws Exception {

    ModifyAwareMap<String, String> map = createMap();
    Assert.assertFalse(map.isMarkedDirty());

    Map<String, String> other = new HashMap<>();
    map.putAll(other);
    Assert.assertTrue(map.isMarkedDirty());
  }

  @Test
  public void testPutAll() throws Exception {

    ModifyAwareMap<String, String> map = createMap();
    Assert.assertFalse(map.isMarkedDirty());

    Map<String, String> other = new HashMap<>();
    other.put("A", "one");
    map.putAll(other);
    Assert.assertTrue(map.isMarkedDirty());
  }

  @Test
  public void testClear() throws Exception {

    ModifyAwareMap<String, String> map = createMap();
    Assert.assertFalse(map.isMarkedDirty());

    map.clear();
    Assert.assertTrue(map.isMarkedDirty());
  }

  @Test
  public void testKeySet() throws Exception {

    ModifyAwareMap<String, String> map = createMap();
    Assert.assertFalse(map.isMarkedDirty());

    Set<String> keys = map.keySet();
    Assert.assertEquals(map.size(), keys.size());
    Assert.assertTrue(keys.contains("A"));
    Assert.assertFalse(map.isMarkedDirty());
  }

  @Test
  public void testValues() throws Exception {

    ModifyAwareMap<String, String> map = createMap();
    Assert.assertFalse(map.isMarkedDirty());

    Collection<String> values = map.values();
    Assert.assertEquals(map.size(), values.size());
    Assert.assertTrue(values.contains("one"));
    Assert.assertFalse(map.isMarkedDirty());
  }

  @Test
  public void testEntrySet() throws Exception {

    ModifyAwareMap<String, String> map = createMap();
    Set<Map.Entry<String, String>> entries = map.entrySet();

    Assert.assertFalse(map.isMarkedDirty());

    Assert.assertEquals(map.size(), entries.size());
    Assert.assertFalse(map.isMarkedDirty());
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
    ModifyAwareMap<String, String> read = (ModifyAwareMap<String, String>)ois.readObject();
    Assertions.assertThat(read).hasSize(orig.size());
  }
}
