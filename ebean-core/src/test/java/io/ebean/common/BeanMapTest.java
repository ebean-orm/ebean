package io.ebean.common;

import io.ebean.bean.BeanCollection;
import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;


public class BeanMapTest {

  Object object1 = new Object();
  Object object2 = new Object();
  Object object3 = new Object();

  private Map<String, Object> all() {
    Map<String, Object> all = new LinkedHashMap<>();
    all.put("1", object1);
    all.put("2", object2);
    all.put("3", object3);
    return all;
  }

  private Map<String, Object> some() {
    Map<String, Object> all = new LinkedHashMap<>();
    all.put("2", object2);
    all.put("3", object3);
    return all;
  }

  @Test
  public void testAdd() throws Exception {

    BeanMap<String, Object> map = new BeanMap<>();
    map.setModifyListening(BeanCollection.ModifyListenMode.ALL);
    map.put("1", object1);
    map.put("4", null);

    assertThat(map.getModifyAdditions()).containsExactly(object1);
    assertThat(map.getModifyRemovals()).isEmpty();

    map.put("1", object1);
    map.put("4", null);
    assertThat(map.getModifyAdditions()).containsExactly(object1);

    map.put("2", object2);
    assertThat(map.getModifyAdditions()).containsExactly(object1, object2);

    map.remove("1");
    assertThat(map.getModifyAdditions()).containsExactly(object2);
    assertThat(map.getModifyRemovals()).isEmpty();
  }

  @Test
  public void testAddAll_given_emptyStart() throws Exception {

    BeanMap<String, Object> set = new BeanMap<>();
    set.setModifyListening(BeanCollection.ModifyListenMode.ALL);

    // act
    set.putAll(all());

    assertThat(set.getModifyAdditions()).containsExactly(object1, object2, object3);
    assertThat(set.getModifyRemovals()).isEmpty();
  }

  @Test
  public void testAdd_given_someAlreadyIn() throws Exception {

    BeanMap<String, Object> map = new BeanMap<>(some());
    map.setModifyListening(BeanCollection.ModifyListenMode.ALL);

    // act
    assertThat(map.values().contains(object1)).isFalse();
    map.put("1", object1);
    assertThat(map.values().contains(object2)).isTrue();
    map.put("2", object2);

    assertThat(map.getModifyAdditions()).containsExactly(object1);
    assertThat(map.getModifyRemovals()).isEmpty();
  }

  @Test
  public void testAddSome_given_someAlreadyIn() throws Exception {

    BeanMap<String, Object> map = new BeanMap<>(some());
    map.setModifyListening(BeanCollection.ModifyListenMode.ALL);

    // act
    map.putAll(all());

    assertThat(map.getModifyAdditions()).containsExactly(object1);
    assertThat(map.getModifyRemovals()).isEmpty();
  }

  @Test
  public void testRemove_given_beansInAdditions() throws Exception {

    BeanMap<String, Object> map = new BeanMap<>();
    map.setModifyListening(BeanCollection.ModifyListenMode.ALL);
    map.putAll(all());
    assertThat(map.getModifyAdditions()).containsExactly(object1, object2, object3);

    // act
    map.remove("2");
    map.remove("3");

    assertThat(map.getModifyAdditions()).containsExactly(object1);
    assertThat(map.getModifyRemovals()).isEmpty();
  }

  @Test
  public void testRemoveAll_given_beansInAdditions() throws Exception {

    BeanMap<String, Object> map = new BeanMap<>();
    map.setModifyListening(BeanCollection.ModifyListenMode.ALL);
    map.putAll(all());
    assertThat(map.getModifyAdditions()).containsExactly(object1, object2, object3);

    // act
    map.remove("2");
    map.remove("3");

    assertThat(map.getModifyAdditions()).containsExactly(object1);
    assertThat(map.getModifyRemovals()).isEmpty();
  }

  @Test
  public void testRemove_given_beansNotInAdditions() throws Exception {

    BeanMap<String, Object> map = new BeanMap<>(all());
    map.setModifyListening(BeanCollection.ModifyListenMode.ALL);

    // act
    map.remove("2");
    map.remove("3");

    // assert
    assertThat(map.getModifyAdditions()).isEmpty();
    assertThat(map.getModifyRemovals()).containsExactly(object2, object3);
  }

  @Test
  public void testRemoveAll_given_beansNotInAdditions() throws Exception {

    BeanMap<String, Object> map = new BeanMap<>(all());
    map.setModifyListening(BeanCollection.ModifyListenMode.ALL);

    // act
    map.remove("2");
    map.remove("3");

    // assert
    assertThat(map.getModifyAdditions()).isEmpty();
    assertThat(map.getModifyRemovals()).containsExactly(object2, object3);
  }

  @Test
  public void testClear() throws Exception {

    BeanMap<String, Object> map = new BeanMap<>(all());
    map.setModifyListening(BeanCollection.ModifyListenMode.ALL);

    // act
    map.clear();

    //assert
    assertThat(map.getModifyRemovals()).containsExactly(object1, object2, object3);
    assertThat(map.getModifyAdditions()).isEmpty();
  }

  @Test
  public void testClear_given_someBeansInAdditions() throws Exception {

    BeanMap<String, Object> map = new BeanMap<>();
    map.put("1", object1);
    map.setModifyListening(BeanCollection.ModifyListenMode.ALL);
    map.put("2", object2);
    map.put("3", object3);

    // act
    map.clear();

    //assert
    assertThat(map.getModifyRemovals()).containsExactly(object1);
    assertThat(map.getModifyAdditions()).isEmpty();
  }

}
