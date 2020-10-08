package org.tests.model.elementcollection;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.annotation.Transactional;
import org.ebeantest.LoggedSqlCollector;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestElementCollectionCascadeMultiple extends BaseTestCase {

  @Test
  public void test() {

    EcTop top = new EcTop("top0");

    EcsPerson person = new EcsPerson("Ethan027");
    person.getPhoneNumbers().add("027 1234");
    person.getPhoneNumbers().add("027 4321");

    top.setPerson(person);
    top.getPeople().add(person);

    LoggedSqlCollector.start();

    save(top);

    final List<String> sql = LoggedSqlCollector.stop();
    assertThat(sql).hasSize(9);

    assertSql(sql.get(0)).contains("insert into ecs_person");
    assertSql(sql.get(1)).contains("-- bind");
    assertSql(sql.get(2)).contains("insert into ec_top");
    assertThat(sql.get(3)).contains("-- bind");
    assertThat(sql.get(4)).contains("insert into ecs_person_phone");
    assertThat(sql.get(5)).contains("-- bind");
    assertThat(sql.get(6)).contains("-- bind");
    assertThat(sql.get(7)).contains("insert into ec_top_ecs_person");
    assertThat(sql.get(8)).contains("-- bind");
  }

  @Transactional(batchSize = 20)
  public void save(EcTop top) {
    DB.save(top);
  }

}
