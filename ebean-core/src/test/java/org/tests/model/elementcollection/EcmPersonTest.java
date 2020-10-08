package org.tests.model.elementcollection;


import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.text.json.JsonContext;
import org.ebeantest.LoggedSqlCollector;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class EcmPersonTest extends BaseTestCase {

  @Test
  public void readJsonThenInsert() {

    EcmPerson source = new EcmPerson("Ethan027");
    source.getPhoneNumbers().put("home", "027 1234");
    source.getPhoneNumbers().put("work", "027 4321");

    final JsonContext json = DB.json();
    final String asJson = json.toJson(source);

    final EcmPerson person = json.toBean(EcmPerson.class, asJson);

    LoggedSqlCollector.start();

    DB.save(person);

    final List<String> sql = LoggedSqlCollector.stop();

    assertThat(sql).hasSize(4);
    assertSql(sql.get(0)).contains("insert into ecm_person ");
    assertSql(sql.get(1)).contains("insert into ecm_person_phone_numbers ");
    assertSql(sql.get(2)).contains("-- bind");
    assertThat(sql.get(3)).contains("-- bind");

    DB.delete(person);
  }
}
