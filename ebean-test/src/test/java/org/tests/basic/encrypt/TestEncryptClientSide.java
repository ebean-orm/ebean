package org.tests.basic.encrypt;

import io.ebean.DB;
import io.ebean.test.LoggedSql;
import io.ebean.xtest.BaseTestCase;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.EBasicEncryptClient;

import java.time.LocalDate;
import java.util.List;

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

    found = DB.find(EBasicEncryptClient.class)
      .where()
      .in("description", "goodbye")
      .in("status", EBasicEncryptClient.Status.TWO)
      .in("dob", today)
      .findOne();
    assertThat(found).isNotNull();
    assertThat(found.getDescription()).isEqualTo("goodbye");


    DB.delete(found);
  }

  @Test
  public void insertUpdate_bindLogMasksEncryptedValues() {
    EBasicEncryptClient bean = new EBasicEncryptClient();
    bean.setName("visible-name");
    bean.setDescription("secret-insert");
    bean.setDob(LocalDate.of(1990, 1, 2));

    LoggedSql.start();
    DB.save(bean);
    bean.setDescription("secret-update");
    DB.save(bean);
    List<String> sql = LoggedSql.stop();

    assertThat(sql).hasSize(2);
    assertThat(sql.get(0)).contains("; -- bind(visible-name,description=****,dob=****,status=****,1)");
    assertThat(sql.get(1)).contains("; -- bind(description=****,2," + bean.getId() + ",1)");

    DB.delete(bean);
  }
}
