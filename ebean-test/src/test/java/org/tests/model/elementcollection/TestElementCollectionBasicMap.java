package org.tests.model.elementcollection;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import io.ebean.test.LoggedSql;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class TestElementCollectionBasicMap extends BaseTestCase {

  @Test
  public void test() {

    LoggedSql.start();

    EcmPerson person = new EcmPerson("MFiona021");
    person.getPhoneNumbers().put("home", "021 1234");
    person.getPhoneNumbers().put("work", "021 4321");
    DB.save(person);

    List<String> sql = LoggedSql.collect();
    if (isPersistBatchOnCascade()) {
      assertThat(sql).hasSize(5);
      assertSql(sql.get(0)).contains("insert into ecm_person");
      assertSql(sql.get(1)).contains("insert into ecm_person_phone");
      assertSqlBind(sql, 2, 3);
      assertSql(sql.get(4)).contains(" -- executeBatch");
    } else {
      assertThat(sql).hasSize(3);
      assertSql(sql.get(0)).contains("insert into ecm_person");
      assertSql(sql.get(1)).contains("insert into ecm_person_phone");
      assertSql(sql.get(2)).contains("insert into ecm_person_phone");
    }

    EcmPerson person1 = new EcmPerson("MFiona09");
    person1.getPhoneNumbers().put("home", "09 1234");
    person1.getPhoneNumbers().put("work", "09 4321");
    person1.getPhoneNumbers().put("mob", "09 9876");
    DB.save(person1);

    LoggedSql.collect();

    List<EcmPerson> found =
      DB.find(EcmPerson.class).where()
        .startsWith("name", "MFiona0")
        .orderBy().asc("id")
        .findList();

    assertThat(found).hasSize(2);

    Map<String, String> phoneNumbers0 = found.get(0).getPhoneNumbers();
    Map<String, String> phoneNumbers1 = found.get(1).getPhoneNumbers();
    phoneNumbers0.size();

    assertThat(phoneNumbers0).containsValues("021 1234", "021 4321");
    assertThat(phoneNumbers0.get("work")).isEqualTo("021 4321");
    assertThat(phoneNumbers1).containsValues("09 1234", "09 4321", "09 9876");
    assertThat(phoneNumbers1.get("mob")).isEqualTo("09 9876");

    sql = LoggedSql.collect();
    assertThat(sql).hasSize(2);
    assertSql(sql.get(0)).contains("select t0.id, t0.name, t0.version from ecm_person t0 where");
    assertSql(sql.get(1)).contains("select t0.ecm_person_id, t0.type, t0.phnum from ecm_person_phone_numbers t0 where");

    List<EcmPerson> found2 =
      DB.find(EcmPerson.class)
        .fetch("phoneNumbers")
        .where()
        .startsWith("name", "MFiona0")
        .orderBy().asc("id")
        .findList();

    assertThat(found2).hasSize(2);

    sql = LoggedSql.collect();
    assertThat(sql).hasSize(1);
    assertSql(sql.get(0)).contains("select t0.id, t0.name, t0.version, t1.type, t1.phnum from ecm_person t0 left join ecm_person_phone_numbers t1");

    EcmPerson foundFirst = found2.get(0);
    jsonToFrom(foundFirst);
    updateBasic(foundFirst);

    LoggedSql.stop();
  }

  private void updateBasic(EcmPerson bean) {

    bean.setName("Fiona021-mod-0");
    DB.save(bean);

    List<String> sql = LoggedSql.collect();
    assertThat(sql).hasSize(1);
    assertSql(sql.get(0)).contains("update ecm_person");

    updateBoth(bean);
  }

  private void updateBoth(EcmPerson bean) {

    bean.setName("Fiona021-mod-both");
    bean.getPhoneNumbers().put("one", "01-22123");
    DB.save(bean);

    List<String> sql = LoggedSql.collect();
    if (isPersistBatchOnCascade()) {
      assertThat(sql).hasSize(9);
      assertSql(sql.get(0)).contains("update ecm_person set name=?, version=? where id=? and version=?");
      assertSql(sql.get(1)).contains("delete from ecm_person_phone_numbers where ecm_person_id=?");
      assertSqlBind(sql.get(2));
      assertThat(sql.get(4)).contains("insert into ecm_person_phone_numbers (ecm_person_id,type,phnum) values (?,?,?)");
      assertSqlBind(sql, 5, 7);
    } else {
      assertThat(sql).hasSize(5);
      assertSql(sql.get(0)).contains("update ecm_person set name=?, version=? where id=? and version=?");
      assertSql(sql.get(1)).contains("delete from ecm_person_phone_numbers where ecm_person_id=?");
      assertSql(sql.get(2)).contains("insert into ecm_person_phone_numbers (ecm_person_id,type,phnum) values (?,?,?)");
      assertThat(sql.get(3)).contains("insert into ecm_person_phone_numbers (ecm_person_id,type,phnum) values (?,?,?)");
      assertThat(sql.get(4)).contains("insert into ecm_person_phone_numbers (ecm_person_id,type,phnum) values (?,?,?)");
    }

    updateNothing(bean);
  }

  private void updateNothing(EcmPerson bean) {

    DB.save(bean);

    List<String> sql = LoggedSql.collect();
    assertThat(sql).hasSize(0);

    updateOnlyCollection(bean);
  }

  private void updateOnlyCollection(EcmPerson bean) {

    bean.getPhoneNumbers().put("two", "01-4321");
    DB.save(bean);

    List<String> sql = LoggedSql.collect();
    if (isPersistBatchOnCascade()) {
      assertThat(sql).hasSize(9);
      assertSql(sql.get(0)).contains("delete from ecm_person_phone_numbers where ecm_person_id=?");
      assertSqlBind(sql.get(1));
      assertSql(sql.get(3)).contains("insert into ecm_person_phone_numbers (ecm_person_id,type,phnum) values (?,?,?)");
      assertSqlBind(sql, 4, 7);
    } else {
      assertThat(sql).hasSize(5);
      assertSql(sql.get(0)).contains("delete from ecm_person_phone_numbers where ecm_person_id=?");
      assertSql(sql.get(1)).contains("insert into ecm_person_phone_numbers (ecm_person_id,type,phnum) values (?,?,?)");
      assertSql(sql.get(2)).contains("insert into ecm_person_phone_numbers (ecm_person_id,type,phnum) values (?,?,?)");
      assertThat(sql.get(3)).contains("insert into ecm_person_phone_numbers (ecm_person_id,type,phnum) values (?,?,?)");
      assertThat(sql.get(4)).contains("insert into ecm_person_phone_numbers (ecm_person_id,type,phnum) values (?,?,?)");
    }

    delete(bean);
  }

  private void delete(EcmPerson bean) {

    DB.delete(bean);

    List<String> sql = LoggedSql.collect();
    assertThat(sql).hasSize(2);
    assertSql(sql.get(0)).contains("delete from ecm_person_phone_numbers where ecm_person_id = ?");
    assertSql(sql.get(1)).contains("delete from ecm_person where id=? and version=?");
  }

  private void jsonToFrom(EcmPerson foundFirst) {
    foundFirst.transientPhoneNumbers = new HashMap<>();
    String asJson = DB.json().toJson(foundFirst);
    EcmPerson fromJson = DB.json().toBean(EcmPerson.class, asJson);

    assertThat(fromJson.getPhoneNumbers()).containsValues("021 1234", "021 4321");
    assertThat(fromJson.getPhoneNumbers().get("home")).isEqualTo("021 1234");
  }

  @Test
  public void json() {

    EcmPerson person = new EcmPerson("Fiona021");
    person.getPhoneNumbers().put("home", "021 1234");
    person.getPhoneNumbers().put("work", "021 4321");

    final String asJson = DB.json().toJson(person);

    assertThat(asJson).isEqualTo("{\"name\":\"Fiona021\",\"phoneNumbers\":{\"home\":\"021 1234\",\"work\":\"021 4321\"}}");

    final EcmPerson fromJson = DB.json().toBean(EcmPerson.class, asJson);
    assertThat(fromJson.getName()).isEqualTo("Fiona021");
    assertThat(fromJson.getPhoneNumbers()).hasSize(2);
    assertThat(fromJson.getPhoneNumbers().toString()).isEqualTo("{home=021 1234, work=021 4321}");
  }
}
