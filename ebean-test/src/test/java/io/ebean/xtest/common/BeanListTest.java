package io.ebean.xtest.common;

import io.ebean.bean.BeanCollection;
import io.ebean.common.BeanList;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


public class BeanListTest {

  private final Object object1 = new Object();
  private final Object object2 = new Object();
  private final Object object3 = new Object();

  private List<Object> all() {
    List<Object> all = new ArrayList<>();
    all.add(object1);
    all.add(object2);
    all.add(object3);
    return all;
  }

  private List<Object> some() {
    List<Object> some = new ArrayList<>();
    some.add(object2);
    some.add(object3);
    return some;
  }

  @Test
  public void test_setModifyListening_null() {

    BeanList<Object> list = new BeanList<>();
    list.setModifyListening(null);

    // act
    list.addAll(all());

    assertThat(list.modifyAdditions()).isNull();
  }

  @Test
  public void test_setModifyListening_none() {

    BeanList<Object> list = new BeanList<>();
    list.setModifyListening(BeanCollection.ModifyListenMode.NONE);

    // act
    list.addAll(all());

    assertThat(list.modifyAdditions()).isNull();
  }

  @Test
  public void testAdd() {

    BeanList<Object> list = new BeanList<>();
    list.setModifyListening(BeanCollection.ModifyListenMode.ALL);
    list.add(object1);

    assertThat(list.modifyAdditions()).containsOnly(object1);
    assertThat(list.modifyRemovals()).isEmpty();

    list.add(object1);
    assertThat(list.modifyAdditions()).containsOnly(object1);

    list.add(object2);
    assertThat(list.modifyAdditions()).containsOnly(object1, object2);

    list.remove(object1);
    assertThat(list.modifyAdditions()).containsOnly(object2);
    assertThat(list.modifyRemovals()).isEmpty();
  }

  @Test
  public void testAddAll_given_emptyStart() {

    BeanList<Object> list = new BeanList<>();
    list.setModifyListening(BeanCollection.ModifyListenMode.ALL);

    // act
    list.addAll(all());

    assertThat(list.modifyAdditions()).containsOnly(object1, object2, object3);
    assertThat(list.modifyRemovals()).isEmpty();
  }

  @Test
  public void test_removals_DeleteThenAddBack_expect_noChange() {

    BeanList<Object> list = new BeanList<>(some());
    list.setModifyListening(BeanCollection.ModifyListenMode.REMOVALS);

    // act
    list.remove(object2);
    assertThat(list.modifyRemovals()).isNotEmpty();
    list.add(object2);

    assertThat(list.modifyRemovals()).isEmpty();
    assertThat(list.modifyAdditions()).isEmpty();
  }

  @Test
  public void test_sort_whenAll_expect_noChange() {

    BeanList<Object> list = new BeanList<>(all());
    list.setModifyListening(BeanCollection.ModifyListenMode.ALL);

    // act
    list.sort(Comparator.comparingInt(Object::hashCode));

    assertThat(list.modifyRemovals()).isEmpty();
    assertThat(list.modifyAdditions()).isEmpty();
  }

  @Test
  public void test_sort_whenRemovals_expect_noChange() {

    BeanList<Object> list = new BeanList<>(all());
    list.setModifyListening(BeanCollection.ModifyListenMode.REMOVALS);

    // act
    list.sort(Comparator.comparingInt(Object::hashCode));

    assertThat(list.modifyRemovals()).isEmpty();
    assertThat(list.modifyAdditions()).isEmpty();
  }

  @Test
  public void testAdd_given_someAlreadyIn() {

    BeanList<Object> list = new BeanList<>(some());
    list.setModifyListening(BeanCollection.ModifyListenMode.ALL);

    // act
    assertThat(list.contains(object1)).isFalse();
    list.add(object1);
    assertThat(list.contains(object2)).isTrue();
    list.add(object2); // object2 added as List allows duplicates

    assertThat(list.modifyAdditions()).containsOnly(object1, object2);
    assertThat(list.modifyRemovals()).isEmpty();
  }

  @Test
  public void testAddSome_given_someAlreadyIn() {

    BeanList<Object> list = new BeanList<>(some());
    list.setModifyListening(BeanCollection.ModifyListenMode.ALL);

    // act
    list.addAll(all());

    assertThat(list.modifyAdditions()).containsOnly(object1, object2, object3);
    assertThat(list.modifyRemovals()).isEmpty();
  }

  @Test
  public void testRemove_given_beansInAdditions() {

    BeanList<Object> list = new BeanList<>();
    list.setModifyListening(BeanCollection.ModifyListenMode.ALL);
    list.addAll(all());
    assertThat(list.modifyAdditions()).containsOnly(object1, object2, object3);

    // act
    list.remove(object2);
    list.remove(object3);

    assertThat(list.modifyAdditions()).containsOnly(object1);
    assertThat(list.modifyRemovals()).isEmpty();
  }

  @Test
  public void testRemoveAll_given_beansInAdditions() {

    BeanList<Object> list = new BeanList<>();
    list.setModifyListening(BeanCollection.ModifyListenMode.ALL);
    list.addAll(all());
    assertThat(list.modifyAdditions()).containsOnly(object1, object2, object3);

    // act
    list.removeAll(some());

    assertThat(list.modifyAdditions()).containsOnly(object1);
    assertThat(list.modifyRemovals()).isEmpty();
  }

  @Test
  public void testRemove_given_beansNotInAdditions() {

    BeanList<Object> list = new BeanList<>(all());
    list.setModifyListening(BeanCollection.ModifyListenMode.ALL);

    // act
    list.remove(object2);
    list.remove(object3);

    // assert
    assertThat(list.modifyAdditions()).isEmpty();
    assertThat(list.modifyRemovals()).containsOnly(object2, object3);
  }

  @Test
  public void testRemoveAll_given_beansNotInAdditions() {

    BeanList<Object> list = new BeanList<>(all());
    list.setModifyListening(BeanCollection.ModifyListenMode.ALL);

    // act
    list.removeAll(some());

    // assert
    assertThat(list.modifyAdditions()).isEmpty();
    assertThat(list.modifyRemovals()).containsOnly(object2, object3);
  }

  @Test
  public void testClear() {

    BeanList<Object> list = new BeanList<>(all());
    list.setModifyListening(BeanCollection.ModifyListenMode.ALL);

    // act
    list.clear();

    //assert
    assertThat(list.modifyRemovals()).containsOnly(object1, object2, object3);
    assertThat(list.modifyAdditions()).isEmpty();
  }

  @Test
  public void testClear_given_someBeansInAdditions() {

    BeanList<Object> list = new BeanList<>();
    list.add(object1);
    list.setModifyListening(BeanCollection.ModifyListenMode.ALL);
    list.add(object2);
    list.add(object3);

    // act
    list.clear();

    //assert
    assertThat(list.modifyRemovals()).containsOnly(object1);
    assertThat(list.modifyAdditions()).isEmpty();
  }

  @Test
  public void testRetainAll_given_beansInAdditions() {

    BeanList<Object> list = new BeanList<>();
    list.setModifyListening(BeanCollection.ModifyListenMode.ALL);
    list.addAll(all());
    assertThat(list.modifyAdditions()).containsOnly(object1, object2, object3);

    // act
    list.retainAll(some());

    assertThat(list.modifyAdditions()).containsOnly(object2, object3);
    assertThat(list.modifyRemovals()).isEmpty();
  }

  @Test
  public void testRetainAll_given_someBeansInAdditions() {

    BeanList<Object> list = new BeanList<>();
    list.add(object1);
    list.add(object2);
    list.setModifyListening(BeanCollection.ModifyListenMode.ALL);
    list.add(object3);

    // act
    list.retainAll(some());

    assertThat(list.modifyAdditions()).containsOnly(object3);
    assertThat(list.modifyRemovals()).containsOnly(object1);
  }

  @Test
  public void testRetainAll_given_noBeansInAdditions() {

    BeanList<Object> list = new BeanList<>(all());
    list.setModifyListening(BeanCollection.ModifyListenMode.ALL);

    // act
    list.retainAll(some());

    assertThat(list.modifyRemovals()).containsOnly(object1);
  }

  @Test
  public void internalAddWithCheck_when_interestingEquals_usesInstanceEquality() {

    BeanList<Object> list = new BeanList<>();
    assertThat(list).hasSize(0);

    SomeBean a = new SomeBean("A");
    list.internalAddWithCheck(a);
    list.internalAddWithCheck(a);
    assertThat(list).hasSize(1);

    // expect to ignore equals and add as it is a diff instance (aka don't use equals())
    list.internalAddWithCheck(new SomeBean("A"));
    assertThat(list).hasSize(2);

    list.internalAddWithCheck(new SomeBean("B"));
    assertThat(list).hasSize(3);
  }

  /**
   * A entity bean with interesting equals implementation.
   */
  private static class SomeBean {

    final String val;

    SomeBean(String val) {
      this.val = val;
    }

    @Override
    public int hashCode() {
      return 42;
    }

    @Override
    public boolean equals(Object other) {
      if (this == other) {
        return true;
      }
      if (other instanceof SomeBean) {
        return this.val.equals(((SomeBean) other).val);
      } else {
        return false;
      }
    }
  }

}
