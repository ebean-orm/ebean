package org.tests.types;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

public class TestEncryptedString extends BaseTestCase {

  @Test
  public void testName() {
    PasswordStoreModel model = new PasswordStoreModel();

    model.setEnc1(EncryptedString.encrypt("Hello"));
    model.setEnc2(EncryptedString.encrypt("World"));
    model.setEnc3(EncryptedString.encrypt("Test"));

    model.setEnc4(EncryptedBinary.encrypt("Hello".getBytes(StandardCharsets.UTF_8)));
    model.setEnc5(EncryptedBinary.encrypt("World".getBytes(StandardCharsets.UTF_8)));
    model.setEnc6(EncryptedBinary.encrypt("Test".getBytes(StandardCharsets.UTF_8)));


    model.save();

    model = Ebean.find(PasswordStoreModel.class, model.getId());

    assertThat(model.getEnc1().getEncryptedData()).isNotEqualTo("Hello");
    assertThat(model.getEnc2().getEncryptedData()).isNotEqualTo("World");
    assertThat(model.getEnc3().getEncryptedData()).isNotEqualTo("Test");
    assertThat(model.getEnc4().getEncryptedData()).isNotEqualTo("Hello".getBytes(StandardCharsets.UTF_8));
    assertThat(model.getEnc5().getEncryptedData()).isNotEqualTo("World".getBytes(StandardCharsets.UTF_8));
    assertThat(model.getEnc6().getEncryptedData()).isNotEqualTo("Test".getBytes(StandardCharsets.UTF_8));


    assertThat(model.getEnc1().decrypt()).isEqualTo("Hello");
    assertThat(model.getEnc2().decrypt()).isEqualTo("World");
    assertThat(model.getEnc3().decrypt()).isEqualTo("Test");
    assertThat(model.getEnc4().decrypt()).isEqualTo("Hello".getBytes(StandardCharsets.UTF_8));
    assertThat(model.getEnc5().decrypt()).isEqualTo("World".getBytes(StandardCharsets.UTF_8));
    assertThat(model.getEnc6().decrypt()).isEqualTo("Test".getBytes(StandardCharsets.UTF_8));

  }
}
