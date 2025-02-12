package org.tests.model.elementcollection;

import io.ebean.DB;
import io.ebean.test.LoggedSql;
import io.ebean.xtest.BaseTestCase;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class TestElementCollectionBasicSet extends BaseTestCase {

  @Test
  void test() {
    LoggedSql.start();

    EcsPerson person = new EcsPerson("Fiona021");
    person.getPhoneNumbers().add("021 1234");
    person.getPhoneNumbers().add("021 4321");
    DB.save(person);

    List<String> sql = LoggedSql.collect();
    if (isPersistBatchOnCascade()) {
      assertThat(sql).hasSize(5);
      assertSql(sql.get(0)).contains("insert into ecs_person");
      assertSql(sql.get(1)).contains("insert into ecs_person_phone");
      assertSqlBind(sql, 2, 3);
    } else {
      assertThat(sql).hasSize(3);
      assertSql(sql.get(0)).contains("insert into ecs_person");
      assertSql(sql.get(1)).contains("insert into ecs_person_phone");
      assertSql(sql.get(2)).contains("insert into ecs_person_phone");
    }

    EcsPerson person1 = new EcsPerson("Fiona09");
    person1.getPhoneNumbers().add("09 1234");
    person1.getPhoneNumbers().add("09 4321");
    person1.getPhoneNumbers().add("09 9876");
    DB.save(person1);

    LoggedSql.collect();

    List<EcsPerson> found =
      DB.find(EcsPerson.class).where()
        .startsWith("name", "Fiona0")
        .orderBy().asc("id")
        .findList();

    Set<String> phoneNumbers0 = found.get(0).getPhoneNumbers();
    Set<String> phoneNumbers1 = found.get(1).getPhoneNumbers();

    sql = LoggedSql.collect();
    assertThat(sql).hasSize(1);
    assertSql(sql.get(0)).contains("select t0.id, t0.name, t0.version from ecs_person t0 where");

    phoneNumbers0.size(); // invoke lazy loading
    sql = LoggedSql.collect();
    assertThat(sql).hasSize(1);
    assertThat(phoneNumbers0).containsExactlyInAnyOrder("021 1234", "021 4321");
    assertThat(phoneNumbers1).containsExactlyInAnyOrder("09 1234", "09 4321", "09 9876");
    assertSql(sql.get(0)).contains("select t0.ecs_person_id, t0.phone from ecs_person_phone t0 where");

    List<EcsPerson> found2 =
      DB.find(EcsPerson.class)
        .fetch("phoneNumbers")
        .where()
        .startsWith("name", "Fiona0")
        .orderBy().asc("id")
        .findList();

    assertThat(found2).hasSize(2);

    sql = LoggedSql.collect();
    assertThat(sql).hasSize(1);
    assertSql(sql.get(0)).contains("select t0.id, t0.name, t0.version, t1.phone from ecs_person t0 left join ecs_person_phone t1");

    EcsPerson foundFirst = found2.get(0);
    jsonToFrom(foundFirst);

    updateBasic(foundFirst);

    LoggedSql.stop();
  }

  private void updateBasic(EcsPerson bean) {

    bean.setName("Fiona021-mod-0");
    DB.save(bean);

    List<String> sql = LoggedSql.collect();
    assertThat(sql).hasSize(1);
    assertSql(sql.get(0)).contains("update ecs_person");

    updateBoth(bean);
  }

  private void updateBoth(EcsPerson bean) {

    bean.setName("Fiona021-mod-both");
    bean.getPhoneNumbers().add("01-22123");
    DB.save(bean);

    List<String> sql = LoggedSql.collect();
    if (isPersistBatchOnCascade()) {
      assertThat(sql).hasSize(9);
      assertSql(sql.get(0)).contains("update ecs_person set name=?, version=? where id=? and version=?");
      assertSql(sql.get(1)).contains("delete from ecs_person_phone where ecs_person_id=?");
      assertSqlBind(sql.get(2));
      assertThat(sql.get(4)).contains("insert into ecs_person_phone (ecs_person_id,phone) values (?,?)");
      assertSqlBind(sql, 5, 7);
    } else {
      assertThat(sql).hasSize(5);
      assertSql(sql.get(0)).contains("update ecs_person set name=?, version=? where id=? and version=?");
      assertSql(sql.get(1)).contains("delete from ecs_person_phone where ecs_person_id=?");
      assertSql(sql.get(2)).contains("insert into ecs_person_phone (ecs_person_id,phone) values (?,?)");
      assertThat(sql.get(3)).contains("insert into ecs_person_phone (ecs_person_id,phone) values (?,?)");
      assertThat(sql.get(4)).contains("insert into ecs_person_phone (ecs_person_id,phone) values (?,?)");
    }

    updateNothing(bean);
  }

  private void updateNothing(EcsPerson bean) {

    DB.save(bean);

    List<String> sql = LoggedSql.collect();
    assertThat(sql).hasSize(0);

    updateOnlyCollection(bean);
  }

  private void updateOnlyCollection(EcsPerson bean) {

    bean.getPhoneNumbers().add("01-4321");
    DB.save(bean);

    List<String> sql = LoggedSql.collect();
    if (isPersistBatchOnCascade()) {
      assertThat(sql).hasSize(9);
      assertSql(sql.get(0)).contains("delete from ecs_person_phone where ecs_person_id=?");
      assertSqlBind(sql.get(1));
      assertSql(sql.get(3)).contains("insert into ecs_person_phone (ecs_person_id,phone) values (?,?)");
      assertSqlBind(sql, 4, 7);
    } else {
      assertThat(sql).hasSize(5);
      assertSql(sql.get(0)).contains("delete from ecs_person_phone where ecs_person_id=?");
      assertSql(sql.get(1)).contains("insert into ecs_person_phone (ecs_person_id,phone) values (?,?)");
      assertSql(sql.get(2)).contains("insert into ecs_person_phone (ecs_person_id,phone) values (?,?)");
      assertThat(sql.get(3)).contains("insert into ecs_person_phone (ecs_person_id,phone) values (?,?)");
      assertThat(sql.get(4)).contains("insert into ecs_person_phone (ecs_person_id,phone) values (?,?)");
    }

    delete(bean);
  }

  private void delete(EcsPerson bean) {

    DB.delete(bean);

    List<String> sql = LoggedSql.collect();
    assertThat(sql).hasSize(2);
    assertSql(sql.get(0)).contains("delete from ecs_person_phone where ecs_person_id = ?");
    assertSql(sql.get(1)).contains("delete from ecs_person where id=? and version=?");
  }

  private void jsonToFrom(EcsPerson foundFirst) {
    String asJson = DB.json().toJson(foundFirst);
    EcsPerson fromJson = DB.json().toBean(EcsPerson.class, asJson);
    assertThat(fromJson.getPhoneNumbers()).containsAll(foundFirst.getPhoneNumbers());
  }

  @Test
  void json() {
    EcsPerson person = new EcsPerson("Fiona021");
    person.getPhoneNumbers().add("021 1234");
    person.getPhoneNumbers().add("021 4321");

    final String asJson = DB.json().toJson(person);

    assertThat(asJson).isEqualTo("{\"name\":\"Fiona021\",\"phoneNumbers\":[\"021 1234\",\"021 4321\"]}");

    final EcsPerson fromJson = DB.json().toBean(EcsPerson.class, asJson);
    assertThat(fromJson.getName()).isEqualTo("Fiona021");
    assertThat(fromJson.getPhoneNumbers()).hasSize(2);
    assertThat(fromJson.getPhoneNumbers().toString()).isEqualTo("[021 1234, 021 4321]");
  }
}
