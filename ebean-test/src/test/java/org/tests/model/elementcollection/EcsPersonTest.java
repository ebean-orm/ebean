package org.tests.model.elementcollection;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import io.ebean.test.LoggedSql;
import io.ebean.text.json.JsonContext;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class EcsPersonTest extends BaseTestCase {

  @Test
  public void jsonReadThenInsert() {

    EcsPerson source = new EcsPerson("Ethan027");
    source.getPhoneNumbers().add("027 1234");
    source.getPhoneNumbers().add("027 4321");

    final JsonContext json = DB.json();
    final String asJson = json.toJson(source);

    final EcsPerson person = json.toBean(EcsPerson.class, asJson);

    LoggedSql.start();

    DB.save(person);

    final List<String> sql = LoggedSql.stop();

    assertThat(sql).hasSize(5);
    assertSql(sql.get(0)).contains("insert into ecs_person ");
    assertSql(sql.get(1)).contains("insert into ecs_person_phone ");
    assertSql(sql.get(2)).contains("-- bind");
    assertThat(sql.get(3)).contains("-- bind");

    DB.delete(person);
  }
}
