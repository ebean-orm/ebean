package com.avaje.ebean.common;

import com.avaje.ebean.bean.BeanCollection;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;


public class BeanListTest {

  Object object1 = new Object();
  Object object2 = new Object();
  Object object3 = new Object();

  @NotNull
  private List<Object> all() {
    List<Object> all = new ArrayList<Object>();
    all.add(object1);
    all.add(object2);
    all.add(object3);
    return all;
  }

  @NotNull
  private List<Object> some() {
    List<Object> some = new ArrayList<Object>();
    some.add(object2);
    some.add(object3);
    return some;
  }

  @Test
  public void testAdd() throws Exception {

    BeanList<Object> list = new BeanList<Object>();
    list.setModifyListening(BeanCollection.ModifyListenMode.ALL);
    list.add(object1);

    assertThat(list.getModifyAdditions()).containsExactly(object1);
    assertThat(list.getModifyRemovals()).isEmpty();

    list.add(object1);
    assertThat(list.getModifyAdditions()).containsExactly(object1);

    list.add(object2);
    assertThat(list.getModifyAdditions()).containsExactly(object1, object2);

    list.remove(object1);
    assertThat(list.getModifyAdditions()).containsExactly(object2);
    assertThat(list.getModifyRemovals()).isEmpty();
  }

  @Test
  public void testAddAll_given_emptyStart() throws Exception {

    BeanList<Object> list = new BeanList<Object>();
    list.setModifyListening(BeanCollection.ModifyListenMode.ALL);

    // act
    list.addAll(all());

    assertThat(list.getModifyAdditions()).containsExactly(object1, object2, object3);
    assertThat(list.getModifyRemovals()).isEmpty();
  }

  @Test
  public void testAdd_given_someAlreadyIn() throws Exception {

    BeanList<Object> list = new BeanList<Object>(some());
    list.setModifyListening(BeanCollection.ModifyListenMode.ALL);

    // act
    assertThat(list.contains(object1)).isFalse();
    list.add(object1);
    assertThat(list.contains(object2)).isTrue();
    list.add(object2); // object2 added as List allows duplicates

    assertThat(list.getModifyAdditions()).containsExactly(object1, object2);
    assertThat(list.getModifyRemovals()).isEmpty();
  }

  @Test
  public void testAddSome_given_someAlreadyIn() throws Exception {

    BeanList<Object> list = new BeanList<Object>(some());
    list.setModifyListening(BeanCollection.ModifyListenMode.ALL);

    // act
    list.addAll(all());

    assertThat(list.getModifyAdditions()).containsExactly(object1, object2, object3);
    assertThat(list.getModifyRemovals()).isEmpty();
  }

  @Test
  public void testRemove_given_beansInAdditions() throws Exception {

    BeanList<Object> list = new BeanList<Object>();
    list.setModifyListening(BeanCollection.ModifyListenMode.ALL);
    list.addAll(all());
    assertThat(list.getModifyAdditions()).containsExactly(object1, object2, object3);

    // act
    list.remove(object2);
    list.remove(object3);

    assertThat(list.getModifyAdditions()).containsExactly(object1);
    assertThat(list.getModifyRemovals()).isEmpty();
  }

  @Test
  public void testRemoveAll_given_beansInAdditions() throws Exception {

    BeanList<Object> list = new BeanList<Object>();
    list.setModifyListening(BeanCollection.ModifyListenMode.ALL);
    list.addAll(all());
    assertThat(list.getModifyAdditions()).containsExactly(object1, object2, object3);

    // act
    list.removeAll(some());

    assertThat(list.getModifyAdditions()).containsExactly(object1);
    assertThat(list.getModifyRemovals()).isEmpty();
  }

  @Test
  public void testRemove_given_beansNotInAdditions() throws Exception {

    BeanList<Object> list = new BeanList<Object>(all());
    list.setModifyListening(BeanCollection.ModifyListenMode.ALL);

    // act
    list.remove(object2);
    list.remove(object3);

    // assert
    assertThat(list.getModifyAdditions()).isEmpty();
    assertThat(list.getModifyRemovals()).containsExactly(object2, object3);
  }

  @Test
  public void testRemoveAll_given_beansNotInAdditions() throws Exception {

    BeanList<Object> list = new BeanList<Object>(all());
    list.setModifyListening(BeanCollection.ModifyListenMode.ALL);

    // act
    list.removeAll(some());

    // assert
    assertThat(list.getModifyAdditions()).isEmpty();
    assertThat(list.getModifyRemovals()).containsExactly(object2, object3);
  }

  @Test
  public void testClear() throws Exception {

    BeanList<Object> list = new BeanList<Object>(all());
    list.setModifyListening(BeanCollection.ModifyListenMode.ALL);

    // act
    list.clear();

    //assert
    assertThat(list.getModifyRemovals()).containsExactly(object1, object2, object3);
    assertThat(list.getModifyAdditions()).isEmpty();
  }

  @Test
  public void testClear_given_someBeansInAdditions() throws Exception {

    BeanList<Object> list = new BeanList<Object>();
    list.add(object1);
    list.setModifyListening(BeanCollection.ModifyListenMode.ALL);
    list.add(object2);
    list.add(object3);

    // act
    list.clear();

    //assert
    assertThat(list.getModifyRemovals()).containsExactly(object1);
    assertThat(list.getModifyAdditions()).isEmpty();
  }

  @Test
  public void testRetainAll_given_beansInAdditions() throws Exception {

    BeanList<Object> list = new BeanList<Object>();
    list.setModifyListening(BeanCollection.ModifyListenMode.ALL);
    list.addAll(all());
    assertThat(list.getModifyAdditions()).containsExactly(object1, object2, object3);

    // act
    list.retainAll(some());

    assertThat(list.getModifyAdditions()).containsExactly(object2, object3);
    assertThat(list.getModifyRemovals()).isEmpty();
  }

  @Test
  public void testRetainAll_given_someBeansInAdditions() throws Exception {

    BeanList<Object> list = new BeanList<Object>();
    list.add(object1);
    list.add(object2);
    list.setModifyListening(BeanCollection.ModifyListenMode.ALL);
    list.add(object3);

    // act
    list.retainAll(some());

    assertThat(list.getModifyAdditions()).containsExactly(object3);
    assertThat(list.getModifyRemovals()).containsExactly(object1);
  }

  @Test
  public void testRetainAll_given_noBeansInAdditions() throws Exception {

    BeanList<Object> list = new BeanList<Object>(all());
    list.setModifyListening(BeanCollection.ModifyListenMode.ALL);

    // act
    list.retainAll(some());

    assertThat(list.getModifyRemovals()).containsExactly(object1);
  }

}