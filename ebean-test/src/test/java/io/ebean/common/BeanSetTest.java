package io.ebean.common;

import io.ebean.bean.BeanCollection;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;


public class BeanSetTest {

  Object object1 = new Object();
  Object object2 = new Object();
  Object object3 = new Object();

  private Set<Object> all() {
    Set<Object> all = new LinkedHashSet<>();
    all.add(object1);
    all.add(object2);
    all.add(object3);
    return all;
  }

  private Set<Object> some() {
    Set<Object> some = new LinkedHashSet<>();
    some.add(object2);
    some.add(object3);
    return some;
  }

  @Test
  public void testAdd() {

    BeanSet<Object> set = new BeanSet<>();
    set.setModifyListening(BeanCollection.ModifyListenMode.ALL);
    set.add(object1);

    assertThat(set.getModifyAdditions()).containsOnly(object1);
    assertThat(set.getModifyRemovals()).isEmpty();

    set.add(object1);
    assertThat(set.getModifyAdditions()).containsOnly(object1);

    set.add(object2);
    assertThat(set.getModifyAdditions()).containsOnly(object1, object2);

    set.remove(object1);
    assertThat(set.getModifyAdditions()).containsOnly(object2);
    assertThat(set.getModifyRemovals()).isEmpty();
  }

  @Test
  public void testAddAll_given_emptyStart() {

    BeanSet<Object> set = new BeanSet<>();
    set.setModifyListening(BeanCollection.ModifyListenMode.ALL);

    // act
    set.addAll(all());

    assertThat(set.getModifyAdditions()).containsOnly(object1, object2, object3);
    assertThat(set.getModifyRemovals()).isEmpty();
  }

  @Test
  public void testAdd_given_someAlreadyIn() {

    BeanSet<Object> set = new BeanSet<>(some());
    set.setModifyListening(BeanCollection.ModifyListenMode.ALL);

    // act
    assertThat(set.contains(object1)).isFalse();
    set.add(object1);
    assertThat(set.contains(object2)).isTrue();
    set.add(object2);

    assertThat(set.getModifyAdditions()).containsOnly(object1);
    assertThat(set.getModifyRemovals()).isEmpty();
  }

  @Test
  public void testAddSome_given_someAlreadyIn() {

    BeanSet<Object> set = new BeanSet<>(some());
    set.setModifyListening(BeanCollection.ModifyListenMode.ALL);

    // act
    set.addAll(all());

    assertThat(set.getModifyAdditions()).containsOnly(object1);
    assertThat(set.getModifyRemovals()).isEmpty();
  }

  @Test
  public void testRemove_given_beansInAdditions() {

    BeanSet<Object> set = new BeanSet<>();
    set.setModifyListening(BeanCollection.ModifyListenMode.ALL);
    set.addAll(all());
    assertThat(set.getModifyAdditions()).containsOnly(object1, object2, object3);

    // act
    set.remove(object2);
    set.remove(object3);

    assertThat(set.getModifyAdditions()).containsOnly(object1);
    assertThat(set.getModifyRemovals()).isEmpty();
  }

  @Test
  public void testRemoveAll_given_beansInAdditions() {

    BeanSet<Object> set = new BeanSet<>();
    set.setModifyListening(BeanCollection.ModifyListenMode.ALL);
    set.addAll(all());
    assertThat(set.getModifyAdditions()).containsOnly(object1, object2, object3);

    // act
    set.removeAll(some());

    assertThat(set.getModifyAdditions()).containsOnly(object1);
    assertThat(set.getModifyRemovals()).isEmpty();
  }

  @Test
  public void testRemove_given_beansNotInAdditions() {

    BeanSet<Object> set = new BeanSet<>(all());
    set.setModifyListening(BeanCollection.ModifyListenMode.ALL);

    // act
    set.remove(object2);
    set.remove(object3);

    // assert
    assertThat(set.getModifyAdditions()).isEmpty();
    assertThat(set.getModifyRemovals()).containsOnly(object2, object3);
  }

  @Test
  public void testRemoveAll_given_beansNotInAdditions() {

    BeanSet<Object> set = new BeanSet<>(all());
    set.setModifyListening(BeanCollection.ModifyListenMode.ALL);

    // act
    set.removeAll(some());

    // assert
    assertThat(set.getModifyAdditions()).isEmpty();
    assertThat(set.getModifyRemovals()).containsOnly(object2, object3);
  }

  @Test
  public void testClear() {

    BeanSet<Object> set = new BeanSet<>(all());
    set.setModifyListening(BeanCollection.ModifyListenMode.ALL);

    // act
    set.clear();

    //assert
    assertThat(set.getModifyRemovals()).containsOnly(object1, object2, object3);
    assertThat(set.getModifyAdditions()).isEmpty();
  }

  @Test
  public void testClear_given_someBeansInAdditions() {

    BeanSet<Object> set = new BeanSet<>();
    set.add(object1);
    set.setModifyListening(BeanCollection.ModifyListenMode.ALL);
    set.add(object2);
    set.add(object3);

    // act
    set.clear();

    //assert
    assertThat(set.getModifyRemovals()).containsOnly(object1);
    assertThat(set.getModifyAdditions()).isEmpty();
  }

  @Test
  public void testRetainAll_given_beansInAdditions() {

    BeanSet<Object> set = new BeanSet<>();
    set.setModifyListening(BeanCollection.ModifyListenMode.ALL);
    set.addAll(all());
    assertThat(set.getModifyAdditions()).containsOnly(object1, object2, object3);

    // act
    set.retainAll(some());

    assertThat(set.getModifyAdditions()).containsOnly(object2, object3);
    assertThat(set.getModifyRemovals()).isEmpty();
  }

  @Test
  public void testRetainAll_given_someBeansInAdditions() {

    BeanSet<Object> set = new BeanSet<>();
    set.add(object1);
    set.add(object2);
    set.setModifyListening(BeanCollection.ModifyListenMode.ALL);
    set.add(object3);

    // act
    set.retainAll(some());

    assertThat(set.getModifyAdditions()).containsOnly(object3);
    assertThat(set.getModifyRemovals()).containsOnly(object1);
  }

  @Test
  public void testRetainAll_given_noBeansInAdditions() {

    BeanSet<Object> set = new BeanSet<>(all());
    set.setModifyListening(BeanCollection.ModifyListenMode.ALL);

    // act
    set.retainAll(some());

    assertThat(set.getModifyRemovals()).containsOnly(object1);
  }

}
