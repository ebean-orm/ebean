package org.tests.model.elementcollection;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import org.ebeantest.LoggedSqlCollector;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestElementCollectionEmbeddedList extends BaseTestCase {

  @Test
  public void test() {

    LoggedSqlCollector.start();

    EcblPerson person = new EcblPerson("Fiona64021");
    person.getPhoneNumbers().add(new EcPhone("64", "021","1234"));
    person.getPhoneNumbers().add(new EcPhone("64","021","4321"));
    Ebean.save(person);

    List<String> sql = LoggedSqlCollector.current();
    assertThat(sql).hasSize(2);
    assertThat(sql.get(0)).contains("insert into ecbl_person");
    assertThat(sql.get(1)).contains("insert into ecbl_person_phone_numbers");

    EcblPerson person1 = new EcblPerson("Fiona6409");
    person1.getPhoneNumbers().add(new EcPhone("61","09","1234"));
    person1.getPhoneNumbers().add(new EcPhone("64","09","4321"));
    Ebean.save(person1);

    LoggedSqlCollector.current();

    List<EcblPerson> found =
      Ebean.find(EcblPerson.class).where()
        .startsWith("name", "Fiona640")
        .order().asc("id")
        .findList();

    List<EcPhone> phoneNumbers0 = found.get(0).getPhoneNumbers();
    List<EcPhone> phoneNumbers1 = found.get(1).getPhoneNumbers();
    phoneNumbers0.size();

    assertThat(phoneNumbers0.toString()).contains("64-021-1234", "64-021-4321");
    assertThat(phoneNumbers1.toString()).contains("61-09-1234", "64-09-4321");

    sql = LoggedSqlCollector.current();
    assertThat(sql).hasSize(2);
    assertThat(trimSql(sql.get(0))).contains("select t0.id, t0.name, t0.version from ecbl_person");
    assertThat(trimSql(sql.get(1))).contains("select t0.person_id, t0.country_code, t0.area, t0.number from ecbl_person_phone_numbers");

    List<EcblPerson> found2 =
      Ebean.find(EcblPerson.class)
        .fetch("phoneNumbers")
        .where()
        .startsWith("name", "Fiona640")
        .order().asc("id")
        .findList();

    assertThat(found2).hasSize(2);

    sql = LoggedSqlCollector.current();
    assertThat(sql).hasSize(1);
    String trimmedSql = trimSql(sql.get(0));
    assertThat(trimmedSql).contains("select t0.id, t0.name, t0.version, t1.country_code, t1.area, t1.number from ecbl_person t0 left join ecbl_person_phone_numbers t1");


    EcblPerson foundFirst = found2.get(0);
    jsonToFrom(foundFirst);

    updateBasic(foundFirst);

    LoggedSqlCollector.stop();
  }

  private void updateBasic(EcblPerson bean) {

    bean.setName("Fiona64-mod-0");
    Ebean.save(bean);

    List<String> sql = LoggedSqlCollector.current();
    assertThat(sql).hasSize(1);
    assertThat(sql.get(0)).contains("update ecbl_person");

    updateBoth(bean);
  }

  private void updateBoth(EcblPerson bean) {

    bean.setName("Fiona64-mod-both");
    bean.getPhoneNumbers().add(new EcPhone("01", "234", "123"));
    Ebean.save(bean);

    List<String> sql = LoggedSqlCollector.current();
    assertThat(sql).hasSize(3);
    assertThat(sql.get(0)).contains("update ecbl_person set name=?, version=? where id=? and version=?");
    assertThat(sql.get(1)).contains("delete from ecbl_person_phone_numbers where person_id=?");
    assertThat(sql.get(2)).contains("insert into ecbl_person_phone_numbers (person_id,country_code,area,number) values (?,?,?,?)");

    updateNothing(bean);
  }

  private void updateNothing(EcblPerson bean) {

    Ebean.save(bean);

    List<String> sql = LoggedSqlCollector.current();
    assertThat(sql).hasSize(0);

    updateOnlyCollection(bean);
  }

  private void updateOnlyCollection(EcblPerson bean) {

    bean.getPhoneNumbers().add(new EcPhone("01", "12", "4321"));
    Ebean.save(bean);

    List<String> sql = LoggedSqlCollector.current();
    assertThat(sql).hasSize(2);
    assertThat(sql.get(0)).contains("delete from ecbl_person_phone_numbers where person_id=?");
    assertThat(sql.get(1)).contains("insert into ecbl_person_phone_numbers (person_id,country_code,area,number) values (?,?,?,?)");

    delete(bean);
  }

  private void delete(EcblPerson bean) {

    Ebean.delete(bean);

    List<String> sql = LoggedSqlCollector.current();
    assertThat(sql).hasSize(2);
    assertThat(sql.get(0)).contains("delete from ecbl_person_phone_numbers where person_id = ?");
    assertThat(sql.get(1)).contains("delete from ecbl_person where id=? and version=?");
  }

  private void jsonToFrom(EcblPerson foundFirst) {

    String asJson = Ebean.json().toJson(foundFirst);

    EcblPerson fromJson = Ebean.json().toBean(EcblPerson.class, asJson);

    String phoneString = fromJson.getPhoneNumbers().toString();
    assertThat(phoneString).contains("64-021-1234");
    assertThat(phoneString).contains("64-021-4321");
  }
}
