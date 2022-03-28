package io.ebeaninternal.server.type;

import io.ebeaninternal.json.ModifyAwareFlag;
import org.junit.jupiter.api.Test;

import java.io.*;

import static org.assertj.core.api.Assertions.assertThat;

public class ModifyAwareFlagTest {

  @Test
  public void serialise() throws IOException, ClassNotFoundException {

    ByteArrayOutputStream os = new ByteArrayOutputStream();
    ObjectOutputStream oos = new ObjectOutputStream(os);

    ModifyAwareFlag flag = new ModifyAwareFlag();
    flag.setMarkedDirty(true);
    oos.writeObject(flag);
    oos.flush();
    oos.close();

    ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
    ObjectInputStream ois = new ObjectInputStream(is);

    ModifyAwareFlag read = (ModifyAwareFlag)ois.readObject();
    assertThat(read.isMarkedDirty()).isTrue();
  }
}
