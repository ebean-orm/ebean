package org.tests.model.elementcollection;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import org.ebeantest.LoggedSqlCollector;
import org.junit.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class TestElementCollectionBasicSet extends BaseTestCase {

  @Test
  public void test() {

    LoggedSqlCollector.start();

    EcsPerson person = new EcsPerson("Fiona021");
    person.getPhoneNumbers().add("021 1234");
    person.getPhoneNumbers().add("021 4321");
    Ebean.save(person);

    List<String> sql = LoggedSqlCollector.current();
    assertThat(sql).hasSize(2);
    assertThat(sql.get(0)).contains("insert into ecs_person");
    assertThat(sql.get(1)).contains("insert into ecs_person_phone");

    EcsPerson person1 = new EcsPerson("Fiona09");
    person1.getPhoneNumbers().add("09 1234");
    person1.getPhoneNumbers().add("09 4321");
    person1.getPhoneNumbers().add("09 9876");
    Ebean.save(person1);

    LoggedSqlCollector.current();

    List<EcsPerson> found =
      Ebean.find(EcsPerson.class).where()
        .startsWith("name", "Fiona0")
        .order().asc("id")
        .findList();

    Set<String> phoneNumbers0 = found.get(0).getPhoneNumbers();
    Set<String> phoneNumbers1 = found.get(1).getPhoneNumbers();
    phoneNumbers0.size();

    assertThat(phoneNumbers0).containsExactly("021 1234", "021 4321");
    assertThat(phoneNumbers1).containsExactly("09 1234", "09 4321", "09 9876");

    sql = LoggedSqlCollector.current();
    assertThat(sql).hasSize(2);
    assertThat(trimSql(sql.get(0))).contains("select t0.id, t0.name, t0.version from ecs_person t0 where");
    assertThat(trimSql(sql.get(1))).contains("select t0.ecs_person_id, t0.phone from ecs_person_phone t0 where");

    List<EcsPerson> found2 =
      Ebean.find(EcsPerson.class)
        .fetch("phoneNumbers")
        .where()
        .startsWith("name", "Fiona0")
        .order().asc("id")
        .findList();

    assertThat(found2).hasSize(2);

    sql = LoggedSqlCollector.current();
    assertThat(sql).hasSize(1);
    assertThat(trimSql(sql.get(0))).contains("select t0.id, t0.name, t0.version, t1.phone from ecs_person t0 left join ecs_person_phone t1");

    EcsPerson foundFirst = found2.get(0);
    jsonToFrom(foundFirst);

    updateBasic(foundFirst);

    LoggedSqlCollector.stop();
  }

  private void updateBasic(EcsPerson bean) {

    bean.setName("Fiona021-mod-0");
    Ebean.save(bean);

    List<String> sql = LoggedSqlCollector.current();
    assertThat(sql).hasSize(1);
    assertThat(sql.get(0)).contains("update ecs_person");

    updateBoth(bean);
  }

  private void updateBoth(EcsPerson bean) {

    bean.setName("Fiona021-mod-both");
    bean.getPhoneNumbers().add("01-22123");
    Ebean.save(bean);

    List<String> sql = LoggedSqlCollector.current();
    assertThat(sql).hasSize(3);
    assertThat(sql.get(0)).contains("update ecs_person set name=?, version=? where id=? and version=?");
    assertThat(sql.get(1)).contains("delete from ecs_person_phone where ecs_person_id=?");
    assertThat(sql.get(2)).contains("insert into ecs_person_phone (ecs_person_id,phone) values (?,?)");

    updateNothing(bean);
  }

  private void updateNothing(EcsPerson bean) {

    Ebean.save(bean);

    List<String> sql = LoggedSqlCollector.current();
    assertThat(sql).hasSize(0);

    updateOnlyCollection(bean);
  }

  private void updateOnlyCollection(EcsPerson bean) {

    bean.getPhoneNumbers().add("01-4321");
    Ebean.save(bean);

    List<String> sql = LoggedSqlCollector.current();
    assertThat(sql).hasSize(2);
    assertThat(sql.get(0)).contains("delete from ecs_person_phone where ecs_person_id=?");
    assertThat(sql.get(1)).contains("insert into ecs_person_phone (ecs_person_id,phone) values (?,?)");

    delete(bean);
  }

  private void delete(EcsPerson bean) {

    Ebean.delete(bean);

    List<String> sql = LoggedSqlCollector.current();
    assertThat(sql).hasSize(2);
    assertThat(sql.get(0)).contains("delete from ecs_person_phone where ecs_person_id = ?");
    assertThat(sql.get(1)).contains("delete from ecs_person where id=? and version=?");
  }

  private void jsonToFrom(EcsPerson foundFirst) {
    String asJson = Ebean.json().toJson(foundFirst);
    EcsPerson fromJson = Ebean.json().toBean(EcsPerson.class, asJson);
    assertThat(fromJson.getPhoneNumbers()).containsAll(foundFirst.getPhoneNumbers());
  }
}
