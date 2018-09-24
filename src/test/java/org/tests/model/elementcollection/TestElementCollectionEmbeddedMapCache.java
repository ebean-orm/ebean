package org.tests.model.elementcollection;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import org.ebeantest.LoggedSqlCollector;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestElementCollectionEmbeddedMapCache extends BaseTestCase {

  @Test
  public void test() {

    EcbmPerson person = new EcbmPerson("Cache1");
    person.getPhoneNumbers().put("home", new EcPhone("64", "021", "1234"));
    person.getPhoneNumbers().put("work", new EcPhone("64", "021", "4321"));
    Ebean.save(person);


    EcbmPerson one = Ebean.find(EcbmPerson.class)
      .setId(person.getId())
      .fetch("phoneNumbers")
      .findOne();

    LoggedSqlCollector.start();

    one.getPhoneNumbers().size();

    List<String> sql = LoggedSqlCollector.current();
    assertThat(sql).isEmpty();

    EcbmPerson two = Ebean.find(EcbmPerson.class)
      .setId(person.getId())
      .findOne();

    two.getPhoneNumbers().size();
    assertThat(two.getPhoneNumbers().toString()).contains("64-021-1234", "64-021-4321");

    sql = LoggedSqlCollector.current();
    assertThat(sql).isEmpty(); // cache hit

    two.getPhoneNumbers().put("mob", new EcPhone("61", "07", "11"));
    two.getPhoneNumbers().remove("home");

    Ebean.save(two);

    sql = LoggedSqlCollector.current();
    assertThat(sql).hasSize(2); // update of collection only
    assertThat(sql.get(0)).contains("delete from ecbm_person_phone_numbers where person_id=?");
    assertThat(sql.get(1)).contains("insert into ecbm_person_phone_numbers (person_id,mkey,country_code,area,number) values (?,?,?,?,?)");

    EcbmPerson three = Ebean.find(EcbmPerson.class)
      .setId(person.getId())
      .findOne();

    assertThat(three.getPhoneNumbers().toString()).contains("61-07-11", "64-021-4321");
    assertThat(three.getPhoneNumbers()).hasSize(2);

    sql = LoggedSqlCollector.current();
    assertThat(sql).isEmpty(); // cache hit


    three.setName("mod-3");
    three.getPhoneNumbers().remove("work");

    Ebean.save(three);

    sql = LoggedSqlCollector.current();
    assertThat(sql).hasSize(3);

    EcbmPerson four = Ebean.find(EcbmPerson.class)
      .setId(person.getId())
      .findOne();

    assertThat(four.getPhoneNumbers().toString()).contains("61-07-11");
    assertThat(four.getPhoneNumbers()).hasSize(1);


    Ebean.delete(four);
    sql = LoggedSqlCollector.current();
    assertThat(sql).hasSize(2);


    LoggedSqlCollector.stop();
  }
}
