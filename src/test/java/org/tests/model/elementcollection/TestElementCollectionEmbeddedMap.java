package org.tests.model.elementcollection;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import org.ebeantest.LoggedSqlCollector;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class TestElementCollectionEmbeddedMap extends BaseTestCase {

  @Test
  public void test() {

    LoggedSqlCollector.start();

    EcbmPerson person = new EcbmPerson("Fiona64021");
    person.getPhoneNumbers().put("home", new EcPhone("64", "021","1234"));
    person.getPhoneNumbers().put("work", new EcPhone("64","021","4321"));
    Ebean.save(person);

    List<String> sql = LoggedSqlCollector.current();
    assertThat(sql).hasSize(2);
    assertThat(sql.get(0)).contains("insert into ecbm_person");
    assertThat(sql.get(1)).contains("insert into ecbm_person_phone_numbers");

    EcbmPerson person1 = new EcbmPerson("Fiona6409");
    person1.getPhoneNumbers().put("home",new EcPhone("64","09","1234"));
    person1.getPhoneNumbers().put("mob",new EcPhone("61","09","4321"));

    Ebean.save(person1);

    LoggedSqlCollector.current();

    List<EcbmPerson> found =
      Ebean.find(EcbmPerson.class).where()
        .startsWith("name", "Fiona640")
        .order().asc("id")
        .findList();

    Map<String,EcPhone> phoneNumbers0 = found.get(0).getPhoneNumbers();
    Map<String,EcPhone> phoneNumbers1 = found.get(1).getPhoneNumbers();
    phoneNumbers0.size();

    assertThat(phoneNumbers0.toString()).contains("64-021-1234", "64-021-4321");
    assertThat(phoneNumbers1.toString()).contains("64-09-1234", "61-09-4321");

    List<EcbmPerson> found2 =
      Ebean.find(EcbmPerson.class)
        .fetch("phoneNumbers")
        .where()
        .startsWith("name", "Fiona640")
        .order().asc("id")
        .findList();

    assertThat(found2).hasSize(2);
    EcbmPerson foundFirst = found2.get(0);

    LoggedSqlCollector.current();

    jsonToFrom(foundFirst);

    updateBasic(foundFirst);

    LoggedSqlCollector.stop();
  }

  private void updateBasic(EcbmPerson bean) {

    bean.setName("Fiona64-mod-0");
    Ebean.save(bean);

    List<String> sql = LoggedSqlCollector.current();
    assertThat(sql).hasSize(1);
    assertThat(sql.get(0)).contains("update ecbm_person");

    updateBoth(bean);
  }

  private void updateBoth(EcbmPerson bean) {

    bean.setName("Fiona64-mod-both");
    bean.getPhoneNumbers().put("more", new EcPhone("01", "234", "123"));
    Ebean.save(bean);

    List<String> sql = LoggedSqlCollector.current();
    assertThat(sql).hasSize(3);
    assertThat(sql.get(0)).contains("update ecbm_person set name=?, version=? where id=? and version=?");
    assertThat(sql.get(1)).contains("delete from ecbm_person_phone_numbers where person_id=?");
    assertThat(sql.get(2)).contains("insert into ecbm_person_phone_numbers (person_id,mkey,country_code,area,number)");

    updateNothing(bean);
  }

  private void updateNothing(EcbmPerson bean) {

    Ebean.save(bean);

    List<String> sql = LoggedSqlCollector.current();
    assertThat(sql).hasSize(0);

    updateOnlyCollection(bean);
  }

  private void updateOnlyCollection(EcbmPerson bean) {

    bean.getPhoneNumbers().put("other", new EcPhone("01", "12", "4321"));
    Ebean.save(bean);

    List<String> sql = LoggedSqlCollector.current();
    assertThat(sql).hasSize(2);
    assertThat(sql.get(0)).contains("delete from ecbm_person_phone_numbers where person_id=?");
    assertThat(sql.get(1)).contains("insert into ecbm_person_phone_numbers (person_id,mkey,country_code,area,number) values (?,?,?,?,?)");

    delete(bean);
  }

  private void delete(EcbmPerson bean) {

    Ebean.delete(bean);

    List<String> sql = LoggedSqlCollector.current();
    assertThat(sql).hasSize(2);
    assertThat(sql.get(0)).contains("delete from ecbm_person_phone_numbers where person_id = ?");
    assertThat(sql.get(1)).contains("delete from ecbm_person where id=? and version=?");
  }

  private void jsonToFrom(EcbmPerson foundFirst) {
    String asJson = Ebean.json().toJson(foundFirst);

    EcbmPerson fromJson = Ebean.json().toBean(EcbmPerson.class, asJson);

    String phoneString = fromJson.getPhoneNumbers().toString();
    assertThat(phoneString).contains("64-021-1234");
    assertThat(phoneString).contains("64-021-4321");
  }

}
