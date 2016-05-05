package com.avaje.ebeaninternal.server.type;

import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;


public class ModifyAwareListTest {

  private ModifyAwareList<String> createList() {
    ArrayList list = new ArrayList();
    list.addAll(Arrays.asList("A","B","C","D","E"));
    return new ModifyAwareList<String>(list);
  }
  private ModifyAwareList<String> createEmptyList() {
    return new ModifyAwareList<String>(new ArrayList<String>());
  }

  @Test
  public void testSize() throws Exception {

    assertEquals(5, createList().size());
  }

  @Test
  public void testIsEmpty() throws Exception {

    assertFalse(createList().isEmpty());
    assertTrue(createEmptyList().isEmpty());
  }

  @Test
  public void testContains() throws Exception {

    assertTrue(createList().contains("B"));
    assertFalse(createList().contains("Z"));
  }

  @Test
  public void testIterator() throws Exception {

    ModifyAwareList<String> list = createList();
    Iterator<String> iterator = list.iterator();
    assertTrue(iterator.hasNext());
    assertEquals("A", iterator.next());
    assertFalse(list.isMarkedDirty());

    iterator.remove();
    assertTrue(list.isMarkedDirty());
  }

  @Test
  public void testToArray() throws Exception {

    Object[] objects = createList().toArray();
    assertEquals(5, objects.length);
    assertEquals("A", objects[0]);
    assertEquals("E", objects[4]);
  }

  @Test
  public void testToArray1() throws Exception {

    String[] objects = createList().toArray(new String[5]);
    assertEquals(5, objects.length);
    assertEquals("A", objects[0]);
    assertEquals("E", objects[4]);
  }

  @Test
  public void testAdd() throws Exception {

    ModifyAwareList<String> list = createList();
    assertFalse(list.isMarkedDirty());

    list.add("F");
    assertTrue(list.isMarkedDirty());
  }

  @Test
  public void testRemove() throws Exception {

    ModifyAwareList<String> list = createList();
    assertFalse(list.isMarkedDirty());

    list.remove("A");
    assertTrue(list.isMarkedDirty());
  }

  @Test
  public void testContainsAll() throws Exception {

    ModifyAwareList<String> list = createList();

    assertTrue(list.containsAll(Arrays.asList("A", "B")));
    assertFalse(list.containsAll(Arrays.asList("A", "B", "Z")));
  }

  @Test
  public void testAddAll() throws Exception {

    ModifyAwareList<String> list = createList();
    assertFalse(list.isMarkedDirty());

    assertTrue(list.addAll(Arrays.asList("F", "G")));
    assertTrue(list.isMarkedDirty());
  }


  @Test
  public void testRemoveAll() throws Exception {

    ModifyAwareList<String> list = createList();
    assertFalse(list.isMarkedDirty());

    assertTrue(list.removeAll(Arrays.asList("A", "G")));
    assertTrue(list.isMarkedDirty());
  }

  @Test
  public void testRetainAll() throws Exception {

    ModifyAwareList<String> list = createList();
    assertFalse(list.isMarkedDirty());

    assertTrue(list.retainAll(Arrays.asList("A", "B")));
    assertTrue(list.isMarkedDirty());
    assertEquals(2, list.size());
  }

  @Test
  public void testClear() throws Exception {

    ModifyAwareList<String> list = createList();
    assertFalse(list.isMarkedDirty());

    list.clear();
    assertTrue(list.isMarkedDirty());
    assertEquals(0, list.size());

  }

  @Test
  public void testGet() throws Exception {

    ModifyAwareList<String> list = createList();

    assertEquals("A", list.get(0));
    assertEquals("B", list.get(1));
    assertEquals("E", list.get(4));
  }

  @Test
  public void testSet() throws Exception {

    ModifyAwareList<String> list = createList();
    assertFalse(list.isMarkedDirty());

    list.set(0, "Z");
    assertTrue(list.isMarkedDirty());
    assertEquals(5, list.size());
  }

  @Test
  public void testIndexOf() throws Exception {

    ModifyAwareList<String> list = createList();
    assertFalse(list.isMarkedDirty());

    assertEquals(2, list.indexOf("C"));
    assertEquals(-1, list.indexOf("Z"));
    assertFalse(list.isMarkedDirty());
  }

  @Test
  public void testLastIndexOf() throws Exception {

    ModifyAwareList<String> list = createList();
    assertFalse(list.isMarkedDirty());

    assertEquals(2, list.lastIndexOf("C"));
    assertEquals(-1, list.lastIndexOf("Z"));
    assertFalse(list.isMarkedDirty());
  }

  @Test
  public void testListIterator() throws Exception {

    ModifyAwareList<String> list = createList();
    ListIterator<String> iterator = list.listIterator();
    assertTrue(iterator.hasNext());
    assertEquals("A", iterator.next());
    assertFalse(list.isMarkedDirty());

    iterator.remove();
    assertTrue(list.isMarkedDirty());
  }

  @Test
  public void testListIterator1() throws Exception {

    ModifyAwareList<String> list = createList();
    ListIterator<String> iterator = list.listIterator(2);
    assertTrue(iterator.hasNext());
    assertEquals("C", iterator.next());
    assertFalse(list.isMarkedDirty());

    iterator.remove();
    assertTrue(list.isMarkedDirty());

  }

  @Test
  public void testSubList() throws Exception {

    ModifyAwareList<String> list = createList();
    List<String> sub = list.subList(1, 3);
    assertEquals("B", sub.get(0));
    assertEquals("C", sub.get(1));

    assertFalse(list.isMarkedDirty());

    sub.remove("C");
    assertTrue(list.isMarkedDirty());
  }

  @Test
  public void testAsSet() throws Exception {

    ModifyAwareList<String> list = createList();
    ModifyAwareSet<String> set = list.asSet();
    assertFalse(set.isMarkedDirty());

    set.add("next");

    assertTrue(set.isMarkedDirty());
  }
}