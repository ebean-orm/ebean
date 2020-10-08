package io.ebeaninternal.server.type;

import io.ebeaninternal.json.ModifyAwareFlag;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import static org.assertj.core.api.Assertions.assertThat;

public class ModifyAwareFlagTest {

  @Test
  public void serialise() throws IOException, ClassNotFoundException {

    ByteArrayOutputStream os = new ByteArrayOutputStream();
    ObjectOutputStream oos = new ObjectOutputStream(os);

    ModifyAwareFlag flag = new ModifyAwareFlag();
    flag.markAsModified();
    oos.writeObject(flag);
    oos.flush();
    oos.close();

    ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
    ObjectInputStream ois = new ObjectInputStream(is);

    ModifyAwareFlag read = (ModifyAwareFlag)ois.readObject();
    assertThat(read.isMarkedDirty()).isTrue();
  }
}
