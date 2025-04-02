package io.ebeaninternal.server.type;

import io.ebeaninternal.json.ModifyAwareList;
import io.ebeaninternal.json.ModifyAwareSet;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;


public class ModifyAwareListTest {

  private ModifyAwareList<String> createList() {
    return new ModifyAwareList<>(new ArrayList<>(Arrays.asList("A", "B", "C", "D", "E")));
  }

  private ModifyAwareList<String> createEmptyList() {
    return new ModifyAwareList<>(new ArrayList<>());
  }

  @Test
  public void testSize() {

    assertEquals(5, createList().size());
  }

  @Test
  public void testIsEmpty() {

    assertFalse(createList().isEmpty());
    assertTrue(createEmptyList().isEmpty());
  }

  @Test
  public void testContains() {

    assertTrue(createList().contains("B"));
    assertFalse(createList().contains("Z"));
  }

  @Test
  public void testIterator() {

    ModifyAwareList<String> list = createList();
    Iterator<String> iterator = list.iterator();
    assertTrue(iterator.hasNext());
    assertEquals("A", iterator.next());
    assertFalse(list.isMarkedDirty());

    iterator.remove();
    assertTrue(list.isMarkedDirty());
  }

  @Test
  public void testToArray() {

    Object[] objects = createList().toArray();
    assertEquals(5, objects.length);
    assertEquals("A", objects[0]);
    assertEquals("E", objects[4]);
  }

  @Test
  public void testToArray1() {

    String[] objects = createList().toArray(new String[5]);
    assertEquals(5, objects.length);
    assertEquals("A", objects[0]);
    assertEquals("E", objects[4]);
  }

  @Test
  public void testAdd() {

    ModifyAwareList<String> list = createList();
    assertFalse(list.isMarkedDirty());

    list.add("F");
    assertTrue(list.isMarkedDirty());
  }

  @Test
  public void testRemove() {

    ModifyAwareList<String> list = createList();
    assertFalse(list.isMarkedDirty());

    list.remove("A");
    assertTrue(list.isMarkedDirty());
  }

  @Test
  public void testContainsAll() {

    ModifyAwareList<String> list = createList();

    assertTrue(list.containsAll(Arrays.asList("A", "B")));
    assertFalse(list.containsAll(Arrays.asList("A", "B", "Z")));
  }

  @Test
  public void testAddAll() {

    ModifyAwareList<String> list = createList();
    assertFalse(list.isMarkedDirty());

    assertTrue(list.addAll(Arrays.asList("F", "G")));
    assertTrue(list.isMarkedDirty());
  }


  @Test
  public void testRemoveAll() {

    ModifyAwareList<String> list = createList();
    assertFalse(list.isMarkedDirty());

    assertTrue(list.removeAll(Arrays.asList("A", "G")));
    assertTrue(list.isMarkedDirty());
  }

  @Test
  public void testRetainAll() {

    ModifyAwareList<String> list = createList();
    assertFalse(list.isMarkedDirty());

    assertTrue(list.retainAll(Arrays.asList("A", "B")));
    assertTrue(list.isMarkedDirty());
    assertEquals(2, list.size());
  }

  @Test
  public void testClear() {

    ModifyAwareList<String> list = createList();
    assertFalse(list.isMarkedDirty());

    list.clear();
    assertTrue(list.isMarkedDirty());
    assertEquals(0, list.size());

  }

  @Test
  public void testGet() {

    ModifyAwareList<String> list = createList();

    assertEquals("A", list.get(0));
    assertEquals("B", list.get(1));
    assertEquals("E", list.get(4));
  }

  @Test
  public void testSet() {

    ModifyAwareList<String> list = createList();
    assertFalse(list.isMarkedDirty());

    list.set(0, "Z");
    assertTrue(list.isMarkedDirty());
    assertEquals(5, list.size());
  }

  @Test
  public void testIndexOf() {

    ModifyAwareList<String> list = createList();
    assertFalse(list.isMarkedDirty());

    assertEquals(2, list.indexOf("C"));
    assertEquals(-1, list.indexOf("Z"));
    assertFalse(list.isMarkedDirty());
  }

  @Test
  public void testLastIndexOf() {

    ModifyAwareList<String> list = createList();
    assertFalse(list.isMarkedDirty());

    assertEquals(2, list.lastIndexOf("C"));
    assertEquals(-1, list.lastIndexOf("Z"));
    assertFalse(list.isMarkedDirty());
  }

  @Test
  public void testListIterator() {

    ModifyAwareList<String> list = createList();
    ListIterator<String> iterator = list.listIterator();
    assertTrue(iterator.hasNext());
    assertEquals("A", iterator.next());
    assertFalse(list.isMarkedDirty());

    iterator.remove();
    assertTrue(list.isMarkedDirty());
  }

  @Test
  public void testListIterator1() {

    ModifyAwareList<String> list = createList();
    ListIterator<String> iterator = list.listIterator(2);
    assertTrue(iterator.hasNext());
    assertEquals("C", iterator.next());
    assertFalse(list.isMarkedDirty());

    iterator.remove();
    assertTrue(list.isMarkedDirty());

  }

  @Test
  public void testSubList() {

    ModifyAwareList<String> list = createList();
    List<String> sub = list.subList(1, 3);
    assertEquals("B", sub.get(0));
    assertEquals("C", sub.get(1));

    assertFalse(list.isMarkedDirty());

    sub.remove("C");
    assertTrue(list.isMarkedDirty());
  }

  @Test
  public void testAsSet() {

    ModifyAwareList<String> list = createList();
    ModifyAwareSet<String> set = list.asSet();
    assertFalse(set.isMarkedDirty());

    set.add("next");

    assertTrue(set.isMarkedDirty());
  }

  @Test
  void freeze() {
    ModifyAwareList<String> orig = createList();
    List<String> frozen = orig.freeze();

    assertThatThrownBy(() -> frozen.add("junk"))
      .isInstanceOf(UnsupportedOperationException.class);
  }

  @Test
  public void freezeAndSerialise() throws IOException, ClassNotFoundException {

    ByteArrayOutputStream os = new ByteArrayOutputStream();
    ObjectOutputStream oos = new ObjectOutputStream(os);

    List<String> orig = createList().freeze();
    oos.writeObject(orig);
    oos.flush();
    oos.close();

    ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
    ObjectInputStream ois = new ObjectInputStream(is);

    @SuppressWarnings("unchecked")
    List<String> read = (List<String>)ois.readObject();
    assertThat(read).contains("A", "B", "C", "D", "E");
    assertThatThrownBy(() -> read.add("junk"))
      .isInstanceOf(UnsupportedOperationException.class);
  }

  @Test
  public void serialise() throws IOException, ClassNotFoundException {

    ByteArrayOutputStream os = new ByteArrayOutputStream();
    ObjectOutputStream oos = new ObjectOutputStream(os);

    ModifyAwareList<String> orig = createList();
    oos.writeObject(orig);
    oos.flush();
    oos.close();

    ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
    ObjectInputStream ois = new ObjectInputStream(is);

    @SuppressWarnings("unchecked")
    ModifyAwareList<String> read = (ModifyAwareList<String>)ois.readObject();
    assertThat(read).contains("A", "B", "C", "D", "E");
  }

  @Test
  public void equalsWhenEqual() {

    ModifyAwareList<String> listA = createList();
    ModifyAwareList<String> listB = createList();

    assertThat(listA).isEqualTo(listB);
    assertThat(listA.hashCode()).isEqualTo(listB.hashCode());
  }

  @Test
  public void equalsWhenNotEqual() {

    ModifyAwareList<String> listA = createList();
    ModifyAwareList<String> listB = createList();
    listB.add("F");

    assertThat(listA).isNotEqualTo(listB);
    assertThat(listA.hashCode()).isNotEqualTo(listB.hashCode());
  }

  @Test
  public void testEqualsAndHashCode() throws Exception {
    ModifyAwareList<String> listA = createEmptyList();
    ArrayList<String> listB = new ArrayList<>();

    assertThat(listA).isEqualTo(listB);
    assertThat(listA.hashCode()).isEqualTo(listB.hashCode());

    listA.add("foo");
    assertThat(listA).isNotEqualTo(listB);
    assertThat(listA.hashCode()).isNotEqualTo(listB.hashCode());

    listB.add("foo");
    assertThat(listA).isEqualTo(listB);
    assertThat(listA.hashCode()).isEqualTo(listB.hashCode());
  }
}
