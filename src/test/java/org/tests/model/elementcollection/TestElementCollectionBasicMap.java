package org.tests.model.elementcollection;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import org.ebeantest.LoggedSqlCollector;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class TestElementCollectionBasicMap extends BaseTestCase {

  @Test
  public void test() {

    LoggedSqlCollector.start();

    EcmPerson person = new EcmPerson("Fiona021");
    person.getPhoneNumbers().put("home", "021 1234");
    person.getPhoneNumbers().put("work", "021 4321");
    Ebean.save(person);

    List<String> sql = LoggedSqlCollector.current();
    assertThat(sql).hasSize(2);
    assertThat(sql.get(0)).contains("insert into ecm_person");
    assertThat(sql.get(1)).contains("insert into ecm_person_phone");

    EcmPerson person1 = new EcmPerson("Fiona09");
    person1.getPhoneNumbers().put("home", "09 1234");
    person1.getPhoneNumbers().put("work", "09 4321");
    person1.getPhoneNumbers().put("mob", "09 9876");
    Ebean.save(person1);

    LoggedSqlCollector.current();

    List<EcmPerson> found =
      Ebean.find(EcmPerson.class).where()
        .startsWith("name", "Fiona0")
        .order().asc("id")
        .findList();

    Map<String, String> phoneNumbers0 = found.get(0).getPhoneNumbers();
    Map<String, String> phoneNumbers1 = found.get(1).getPhoneNumbers();
    phoneNumbers0.size();

    assertThat(phoneNumbers0).containsValues("021 1234", "021 4321");
    assertThat(phoneNumbers0.get("work")).isEqualTo("021 4321");
    assertThat(phoneNumbers1).containsValues("09 1234", "09 4321", "09 9876");
    assertThat(phoneNumbers1.get("mob")).isEqualTo("09 9876");

    sql = LoggedSqlCollector.current();
    assertThat(sql).hasSize(2);
    assertThat(trimSql(sql.get(0))).contains("select t0.id, t0.name, t0.version from ecm_person t0 where");
    assertThat(trimSql(sql.get(1))).contains("select t0.ecm_person_id, t0.type, t0.number from ecm_person_phone_numbers t0 where");

    List<EcmPerson> found2 =
      Ebean.find(EcmPerson.class)
        .fetch("phoneNumbers")
        .where()
        .startsWith("name", "Fiona0")
        .order().asc("id")
        .findList();

    assertThat(found2).hasSize(2);

    sql = LoggedSqlCollector.current();
    assertThat(sql).hasSize(1);
    assertThat(trimSql(sql.get(0))).contains("select t0.id, t0.name, t0.version, t1.type, t1.number from ecm_person t0 left join ecm_person_phone_numbers t1");

    EcmPerson foundFirst = found2.get(0);
    jsonToFrom(foundFirst);
    updateBasic(foundFirst);

    LoggedSqlCollector.stop();
  }

  private void updateBasic(EcmPerson bean) {

    bean.setName("Fiona021-mod-0");
    Ebean.save(bean);

    List<String> sql = LoggedSqlCollector.current();
    assertThat(sql).hasSize(1);
    assertThat(sql.get(0)).contains("update ecm_person");

    updateBoth(bean);
  }

  private void updateBoth(EcmPerson bean) {

    bean.setName("Fiona021-mod-both");
    bean.getPhoneNumbers().put("one", "01-22123");
    Ebean.save(bean);

    List<String> sql = LoggedSqlCollector.current();
    assertThat(sql).hasSize(3);
    assertThat(sql.get(0)).contains("update ecm_person set name=?, version=? where id=? and version=?");
    assertThat(sql.get(1)).contains("delete from ecm_person_phone_numbers where ecm_person_id=?");
    assertThat(sql.get(2)).contains("insert into ecm_person_phone_numbers (ecm_person_id,type,number) values (?,?,?)");

    updateNothing(bean);
  }

  private void updateNothing(EcmPerson bean) {

    Ebean.save(bean);

    List<String> sql = LoggedSqlCollector.current();
    assertThat(sql).hasSize(0);

    updateOnlyCollection(bean);
  }

  private void updateOnlyCollection(EcmPerson bean) {

    bean.getPhoneNumbers().put("two", "01-4321");
    Ebean.save(bean);

    List<String> sql = LoggedSqlCollector.current();
    assertThat(sql).hasSize(2);
    assertThat(sql.get(0)).contains("delete from ecm_person_phone_numbers where ecm_person_id=?");
    assertThat(sql.get(1)).contains("insert into ecm_person_phone_numbers (ecm_person_id,type,number) values (?,?,?)");

    delete(bean);
  }

  private void delete(EcmPerson bean) {

    Ebean.delete(bean);

    List<String> sql = LoggedSqlCollector.current();
    assertThat(sql).hasSize(2);
    assertThat(sql.get(0)).contains("delete from ecm_person_phone_numbers where ecm_person_id = ?");
    assertThat(sql.get(1)).contains("delete from ecm_person where id=? and version=?");
  }

  private void jsonToFrom(EcmPerson foundFirst) {
    foundFirst.transientPhoneNumbers = new HashMap<>();
    String asJson = Ebean.json().toJson(foundFirst);
    EcmPerson fromJson = Ebean.json().toBean(EcmPerson.class, asJson);

    assertThat(fromJson.getPhoneNumbers()).containsValues("021 1234", "021 4321");
    assertThat(fromJson.getPhoneNumbers().get("home")).isEqualTo("021 1234");
  }
}
