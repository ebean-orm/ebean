package io.ebeaninternal.server.type;

import io.ebeaninternal.json.ModifyAwareMap;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ModifyAwareMapTest {

  private ModifyAwareMap<String, Integer> createMap() {
    HashMap<String, Integer> set = new HashMap<>();
    set.put("A", 1);
    set.put("B", 2);
    set.put("C", 3);
    set.put("D", 4);
    set.put("E", 5);
    return new ModifyAwareMap<>(set);
  }

  private ModifyAwareMap<String, Integer> createEmptyMap() {
    HashMap<String, Integer> set = new HashMap<>();
    return new ModifyAwareMap<>(set);
  }

  @Test
  void freeze() {
    Map<String, Integer> frozen = createMap().freeze();
    assertThatThrownBy(() -> frozen.put("junk", 1))
      .isInstanceOf(UnsupportedOperationException.class);
  }

  @Test
  public void serialise() throws IOException, ClassNotFoundException {

    ByteArrayOutputStream os = new ByteArrayOutputStream();
    ObjectOutputStream oos = new ObjectOutputStream(os);

    oos.writeObject(createMap());
    oos.flush();
    oos.close();

    ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
    ObjectInputStream ois = new ObjectInputStream(is);

    @SuppressWarnings("unchecked")
    ModifyAwareMap<String, Integer> read = (ModifyAwareMap<String, Integer>) ois.readObject();
    assertThat(read).containsKeys("A", "B", "C", "D", "E").containsValues(1, 2, 3, 4, 5);
  }

  @Test
  public void equalsWhenEqual() {

    ModifyAwareMap<String, Integer> setA = createMap();
    ModifyAwareMap<String, Integer> setB = createMap();

    assertThat(setA).isEqualTo(setB);
    assertThat(setA.hashCode()).isEqualTo(setB.hashCode());
  }

  @Test
  public void equalsWhenNotEqual() {

    ModifyAwareMap<String, Integer> setA = createMap();
    ModifyAwareMap<String, Integer> setB = createMap();
    setB.put("F", 6);

    assertThat(setA).isNotEqualTo(setB);
    assertThat(setA.hashCode()).isNotEqualTo(setB.hashCode());
  }

  @Test
  public void testEqualsAndHashCode() throws Exception {
    ModifyAwareMap<String, Integer> setA = createEmptyMap();
    HashMap<String, Integer> setB = new HashMap<>();

    assertThat(setA).isEqualTo(setB);
    assertThat(setA.hashCode()).isEqualTo(setB.hashCode());

    setA.put("foo", 42);
    assertThat(setA).isNotEqualTo(setB);
    assertThat(setA.hashCode()).isNotEqualTo(setB.hashCode());

    setB.put("foo", 42);
    assertThat(setA).isEqualTo(setB);
    assertThat(setA.hashCode()).isEqualTo(setB.hashCode());
  }
}
