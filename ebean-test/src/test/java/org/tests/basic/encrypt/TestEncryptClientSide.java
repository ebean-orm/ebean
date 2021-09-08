package org.tests.basic.encrypt;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.EBasicEncryptClient;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

public class TestEncryptClientSide extends BaseTestCase {

  @Test
  public void insertUpdate() {

    LocalDate today = LocalDate.now();

    EBasicEncryptClient bean = new EBasicEncryptClient();
    bean.setDescription("hello");
    bean.setStatus(EBasicEncryptClient.Status.ONE);
    bean.setDob(today);

    DB.save(bean);

    EBasicEncryptClient found = DB.find(EBasicEncryptClient.class)
      .where()
      .eq("description", "hello")
      .eq("status", EBasicEncryptClient.Status.ONE)
      .eq("dob", today)
      .findOne();

    assertThat(found).isNotNull();
    assertThat(found.getDescription()).isEqualTo("hello");
    assertThat(found.getStatus()).isEqualTo(EBasicEncryptClient.Status.ONE);
    assertThat(found.getDob()).isEqualTo(today);


    found.setDescription("goodbye");
    found.setStatus(EBasicEncryptClient.Status.TWO);
    DB.save(found);

    found = DB.find(EBasicEncryptClient.class)
      .where()
      .eq("description", "goodbye")
      .eq("status", EBasicEncryptClient.Status.TWO)
      .eq("dob", today)
      .findOne();

    assertThat(found).isNotNull();
    assertThat(found.getDescription()).isEqualTo("goodbye");

    DB.delete(found);
  }
}
