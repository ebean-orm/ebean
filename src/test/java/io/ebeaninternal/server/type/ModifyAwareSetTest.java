package io.ebeaninternal.server.type;

import io.ebeaninternal.json.ModifyAwareSet;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;

public class ModifyAwareSetTest {

  private ModifyAwareSet<String> createSet() {
    HashSet<String> set = new HashSet<>();
    set.addAll(Arrays.asList("A", "B", "C", "D", "E"));
    return new ModifyAwareSet<>(set);
  }

  @Test
  public void serialise() throws IOException, ClassNotFoundException {

    ByteArrayOutputStream os = new ByteArrayOutputStream();
    ObjectOutputStream oos = new ObjectOutputStream(os);

    oos.writeObject(createSet());
    oos.flush();
    oos.close();

    ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
    ObjectInputStream ois = new ObjectInputStream(is);

    @SuppressWarnings("unchecked")
    ModifyAwareSet<String> read = (ModifyAwareSet<String>)ois.readObject();
    assertThat(read).contains("A", "B", "C", "D", "E");
  }

  @Test
  public void equalsWhenEqual() {

    ModifyAwareSet<String> setA = createSet();
    ModifyAwareSet<String> setB = createSet();

    assertThat(setA).isEqualTo(setB);
    assertThat(setA.hashCode()).isEqualTo(setB.hashCode());
  }

  @Test
  public void equalsWhenNotEqual() {

    ModifyAwareSet<String> setA = createSet();
    ModifyAwareSet<String> setB = createSet();
    setB.add("F");

    assertThat(setA).isNotEqualTo(setB);
    assertThat(setA.hashCode()).isNotEqualTo(setB.hashCode());
  }
}
