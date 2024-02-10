package io.ebean.xtest.common;

import io.ebean.bean.BeanCollection;
import io.ebean.common.BeanMap;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Product;

import java.util.*;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;


public class BeanMapTest {

  private final Product object1 = new Product(1);
  private final Product object2 = new Product(2);
  private final Product object3 = new Product(3);
  private final Product object4 = new Product(4);
  private final Product object5 = new Product(5);

  private Map<String, Object> all() {
    Map<String, Object> all = new LinkedHashMap<>();
    all.put("1", new Product(1));
    all.put("2", new Product(2));
    all.put("3", new Product(3));
    return all;
  }

  private Map<String, Object> some() {
    Map<String, Object> all = new LinkedHashMap<>();
    all.put("2", new Product(2));
    all.put("3", new Product(3));
    return all;
  }

  @Test
  public void testAdd() {

    BeanMap<String, Object> map = new BeanMap<>();
    map.setModifyListening(BeanCollection.ModifyListenMode.ALL);
    map.put("1", object1);
    map.put("4", null);

    assertThat(map.modifyAdditions()).containsOnly(object1);
    assertThat(map.modifyRemovals()).isEmpty();

    map.put("1", object1);
    map.put("4", null);
    assertThat(map.modifyAdditions()).containsOnly(object1);

    map.put("2", object2);
    assertThat(map.modifyAdditions()).containsOnly(object1, object2);

    map.remove("1");
    assertThat(map.modifyAdditions()).containsOnly(object2);
    assertThat(map.modifyRemovals()).isEmpty();
  }

  @Test
  public void testAddAll_given_emptyStart() {

    BeanMap<String, Object> set = new BeanMap<>();
    set.setModifyListening(BeanCollection.ModifyListenMode.ALL);

    // act
    set.putAll(all());

    assertThat(set.modifyAdditions()).containsOnly(object1, object2, object3);
    assertThat(set.modifyRemovals()).isEmpty();
  }

  @Test
  public void testAdd_given_someAlreadyIn() {
    Map<String, Object> some = new LinkedHashMap<>(Map.of("2", object2, "3", object3));
    BeanMap<String, Object> map = new BeanMap<>(some);
    map.setModifyListening(BeanCollection.ModifyListenMode.ALL);

    // act
    assertThat(map.containsValue(object1)).isFalse();
    map.put("1", object1);
    assertThat(map.containsValue(object2)).isTrue();
    map.put("2", object2);

    assertThat(map.modifyAdditions()).containsOnly(object1);
    assertThat(map.modifyRemovals()).isEmpty();
  }

  @Test
  public void testAddSome_given_someAlreadyIn() {

    Map<String, Object> some = new LinkedHashMap<>(Map.of("2", object2, "3", object3));
    BeanMap<String, Object> map = new BeanMap<>(some);
    map.setModifyListening(BeanCollection.ModifyListenMode.ALL);

    // act
    Map<String, Object> all = Map.of("1", object1, "2", object2, "3", object3);
    map.putAll(all);

    assertThat(map.modifyAdditions()).containsOnly(object1);
    assertThat(map.modifyRemovals()).isEmpty();
  }

  @Test
  public void testRemove_given_beansInAdditions() {

    BeanMap<String, Object> map = new BeanMap<>();
    map.setModifyListening(BeanCollection.ModifyListenMode.ALL);
    map.putAll(all());
    assertThat(map.modifyAdditions()).containsOnly(object1, object2, object3);

    // act
    map.remove("2");
    map.remove("3");

    assertThat(map.modifyAdditions()).containsOnly(object1);
    assertThat(map.modifyRemovals()).isEmpty();
  }

  @Test
  public void testRemoveAll_given_beansInAdditions() {

    BeanMap<String, Object> map = new BeanMap<>();
    map.setModifyListening(BeanCollection.ModifyListenMode.ALL);
    map.putAll(all());
    assertThat(map.modifyAdditions()).containsOnly(object1, object2, object3);

    // act
    map.remove("2");
    map.remove("3");

    assertThat(map.modifyAdditions()).containsOnly(object1);
    assertThat(map.modifyRemovals()).isEmpty();
  }

  @Test
  public void testRemove_given_beansNotInAdditions() {

    BeanMap<String, Object> map = new BeanMap<>(all());
    map.setModifyListening(BeanCollection.ModifyListenMode.ALL);

    // act
    map.remove("2");
    map.remove("3");

    // assert
    assertThat(map.modifyAdditions()).isEmpty();
    assertThat(map.modifyRemovals()).containsOnly(object2, object3);
  }

  @Test
  public void testRemoveAll_given_beansNotInAdditions() {

    BeanMap<String, Object> map = new BeanMap<>(all());
    map.setModifyListening(BeanCollection.ModifyListenMode.ALL);

    // act
    map.remove("2");
    map.remove("3");

    // assert
    assertThat(map.modifyAdditions()).isEmpty();
    assertThat(map.modifyRemovals()).containsOnly(object2, object3);
  }

  @Test
  public void testClear() {

    BeanMap<String, Object> map = new BeanMap<>(all());
    map.setModifyListening(BeanCollection.ModifyListenMode.ALL);

    // act
    map.clear();

    //assert
    assertThat(map.modifyRemovals()).containsOnly(object1, object2, object3);
    assertThat(map.modifyAdditions()).isEmpty();
  }

  @Test
  public void testClear_given_someBeansInAdditions() {

    BeanMap<String, Product> map = newModifyListeningMap();
    map.put("2", object2);
    map.put("3", object3);

    // act
    map.clear();

    //assert
    assertThat(map.modifyRemovals()).containsOnly(object1);
    assertThat(map.modifyAdditions()).isEmpty();
  }

  @Test
  public void keySet_add_whenModifyListening() {
    BeanMap<String, Product> map = newModifyListeningMap();
    assertThrows(UnsupportedOperationException.class, () -> map.keySet().add("3"));
  }

  @Test
  public void keySet_add() {
    BeanMap<String, Object> map = new BeanMap<>();
    assertThrows(UnsupportedOperationException.class, () ->map.keySet().add("3"));
  }

  @Test
  public void keySet_addAll_whenModifyListening() {
    BeanMap<String, Product> map = newModifyListeningMap();
    assertThrows(UnsupportedOperationException.class, () -> map.keySet().addAll(asList("3", "4")));
  }

  @Test
  public void keySet_addAll() {
    BeanMap<String, Object> map = new BeanMap<>();
    assertThrows(UnsupportedOperationException.class, () -> map.keySet().addAll(asList("3", "4")));
  }

  @Test
  public void keySet_remove() {
    BeanMap<String, Object> map = new BeanMap<>();
    map.put("1", object1);
    map.put("2", object2);
    map.setModifyListening(BeanCollection.ModifyListenMode.ALL);

    final Set<String> keySet = map.keySet();
    keySet.remove("1");

    assertThat(keySet.contains("1")).isFalse();
    assertThat(map).doesNotContainKeys("1");
    assertThat(map.get("1")).isNull();

    assertThat(map.modifyRemovals()).containsOnly(object1);
  }

  @Test
  public void keySet_clear() {
    BeanMap<String, Object> map = new BeanMap<>();
    map.put("1", object1);
    map.put("2", object2);
    map.setModifyListening(BeanCollection.ModifyListenMode.ALL);

    final Set<String> keySet = map.keySet();
    keySet.clear();

    assertThat(map).isEmpty();
    assertThat(keySet).isEmpty();

    assertThat(map.modifyRemovals()).containsOnly(object1, object2);
  }

  @Test
  public void keySet_iterator_remove() {

    BeanMap<String, Object> map = new BeanMap<>();
    map.put("1", object1);
    map.put("2", object2);
    map.put("3", object3);
    map.setModifyListening(BeanCollection.ModifyListenMode.ALL);

    final Set<String> keySet = map.keySet();
    keySet.removeIf(key -> key.equals("2"));

    assertThat(map).hasSize(2);
    assertThat(keySet).hasSize(2);
    assertThat(keySet).containsExactly("1", "3");
    assertThat(map).containsKeys("1", "3");

    assertThat(map.modifyRemovals()).containsOnly(object2);
  }

  @Test
  public void keySet_removeAll() {

    BeanMap<String, Object> map = new BeanMap<>();
    map.put("1", object1);
    map.put("2", object2);
    map.put("3", object3);
    map.put("4", object4);
    map.put("5", object5);
    map.setModifyListening(BeanCollection.ModifyListenMode.ALL);

    final Set<String> keySet = map.keySet();
    final boolean changed = keySet.removeAll(asList("2", "3", "5"));

    assertThat(changed).isTrue();
    assertThat(map).hasSize(2);
    assertThat(keySet).hasSize(2);
    assertThat(keySet).containsExactly("1", "4");
    assertThat(map).containsKeys("1", "4");

    assertThat(map.modifyRemovals()).containsOnly(object2, object3, object5);
  }


  @Test
  public void keySet_retainAll() {

    BeanMap<String, Object> map = new BeanMap<>();
    map.put("1", object1);
    map.put("2", object2);
    map.put("3", object3);
    map.put("4", object4);
    map.put("5", object5);
    map.setModifyListening(BeanCollection.ModifyListenMode.ALL);

    final Set<String> keySet = map.keySet();
    final boolean changed = keySet.retainAll(asList("2", "3", "5"));

    assertThat(changed).isTrue();
    assertThat(map).hasSize(3);
    assertThat(keySet).hasSize(3);
    assertThat(keySet).containsExactly("2", "3", "5");
    assertThat(map).containsKeys("2", "3", "5");

    assertThat(map.modifyRemovals()).containsOnly(object1, object4);
  }

  @Test
  public void values_add() {
    BeanMap<String, Product> map = new BeanMap<>();
    assertThrows(UnsupportedOperationException.class, () -> map.values().add(object3));
  }

  @Test
  public void values_addAll() {
    BeanMap<String, Product> map = new BeanMap<>();
    assertThrows(UnsupportedOperationException.class, () -> map.values().addAll(asList(object3, object5)));
  }

  @Test
  public void entrySet_add() {
    assertThrows(UnsupportedOperationException.class, () ->
      newModifyListeningMap()
      .entrySet()
      .add(new AbstractMap.SimpleEntry<>("3", object3)));
  }

  @Test
  public void entrySet_clear() {
    final BeanMap<String, Product> map = newModifyListeningMap();
    final Set<Map.Entry<String, Product>> entries = map.entrySet();
    entries.clear();

    assertThat(entries).isEmpty();
    assertThat(map).isEmpty();
    assertThat(map.modifyRemovals()).containsOnly(object1);
  }

  @Test
  public void entrySet_remove() {
    final BeanMap<String, Product> map = newModifyListeningMap5();
    final Set<Map.Entry<String, Product>> entries = map.entrySet();

    assertThat(map).hasSize(5);

    final boolean existed1 = entries.remove(new AbstractMap.SimpleEntry<>("1", object1));
    assertThat(existed1).isTrue();

    final boolean existed22 = entries.remove(new AbstractMap.SimpleEntry<>("22", object1));
    assertThat(existed22).isFalse();

    assertThat(map).hasSize(4);
    assertThat(map.modifyRemovals()).containsOnly(object1);
  }

  @Test
  public void entrySet_remove_whenNotEqualValue() {
    final BeanMap<String, Product> map = newModifyListeningMap5();
    final Set<Map.Entry<String, Product>> entries = map.entrySet();

    assertThat(map).hasSize(5);

    final boolean modified = entries.remove(new AbstractMap.SimpleEntry<>("1", object2));
    assertThat(modified).isFalse();

    assertThat(map).hasSize(5);
    assertThat(map.modifyRemovals()).isNull();
  }

  @Test
  public void entrySet_iterator_remove() {
    final BeanMap<String, Product> map = newModifyListeningMap5();
    final Set<Map.Entry<String, Product>> entries = map.entrySet();
    final Iterator<Map.Entry<String, Product>> iterator = entries.iterator();
    while (iterator.hasNext()) {
      final Map.Entry<String, Product> entry = iterator.next();
      if (entry.getKey().equals("2") || entry.getKey().equals("5")) {
        iterator.remove();
      }
    }
    assertThat(map).hasSize(3);
    assertThat(entries).hasSize(3);
    assertThat(map.modifyRemovals()).containsOnly(object2, object5);
  }

  @Test
  public void entrySet_removeAll() {
    final BeanMap<String, Product> map = newModifyListeningMap5();
    final Set<Map.Entry<String, Product>> entries = map.entrySet();

    entries.removeAll(asList(new AbstractMap.SimpleEntry<>("1", object1), new AbstractMap.SimpleEntry<>("3", object4), new AbstractMap.SimpleEntry<>("4", object4)));
    assertThat(map).hasSize(3);
    assertThat(entries).hasSize(3);
    assertThat(map.modifyRemovals()).containsOnly(object1, object4);
  }

  @Test
  public void entrySet_retainAll() {
    final BeanMap<String, Product> map = newModifyListeningMap5();
    final Set<Map.Entry<String, Product>> entries = map.entrySet();

    entries.retainAll(asList(new AbstractMap.SimpleEntry<>("1", object1), new AbstractMap.SimpleEntry<>("3", object4), new AbstractMap.SimpleEntry<>("4", object4)));
    assertThat(map).hasSize(2);
    assertThat(entries).hasSize(2);
    assertThat(map.modifyRemovals()).containsOnly(object2, object3, object5);
  }

  private BeanMap<String, Product> newModifyListeningMap() {
    BeanMap<String, Product> map = new BeanMap<>();
    map.put("1", object1);
    map.setModifyListening(BeanCollection.ModifyListenMode.ALL);
    return map;
  }

  private BeanMap<String, Product> newModifyListeningMap5() {
    BeanMap<String, Product> map = new BeanMap<>();
    map.put("1", object1);
    map.put("2", object2);
    map.put("3", object3);
    map.put("4", object4);
    map.put("5", object5);
    map.setModifyListening(BeanCollection.ModifyListenMode.ALL);
    return map;
  }
}
