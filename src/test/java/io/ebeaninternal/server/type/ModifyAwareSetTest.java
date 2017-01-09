package io.ebeaninternal.server.type;

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

    ModifyAwareSet<String> read = (ModifyAwareSet<String>)ois.readObject();
    assertThat(read).contains("A", "B", "C", "D", "E");
  }
}
