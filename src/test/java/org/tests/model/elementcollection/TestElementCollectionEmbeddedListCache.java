package org.tests.model.elementcollection;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import org.ebeantest.LoggedSqlCollector;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestElementCollectionEmbeddedListCache extends BaseTestCase {

  @Test
  public void test() {

    EcblPerson person = new EcblPerson("CacheL");
    person.getPhoneNumbers().add(new EcPhone("64", "021","1234"));
    person.getPhoneNumbers().add(new EcPhone("64","021","4321"));
    Ebean.save(person);

    EcblPerson one = Ebean.find(EcblPerson.class)
      .setId(person.getId())
      .fetch("phoneNumbers")
      .findOne();

    LoggedSqlCollector.start();

    one.getPhoneNumbers().size();

    List<String> sql = LoggedSqlCollector.current();
    assertThat(sql).isEmpty();

    EcblPerson two = Ebean.find(EcblPerson.class)
      .setId(person.getId())
      .findOne();

    two.getPhoneNumbers().size();
    assertThat(two.getPhoneNumbers().toString()).contains("64-021-1234", "64-021-4321");

    sql = LoggedSqlCollector.current();
    assertThat(sql).isEmpty(); // cache hit

    two.getPhoneNumbers().add(new EcPhone("61", "07", "11"));
    two.getPhoneNumbers().remove(1);

    Ebean.save(two);

    sql = LoggedSqlCollector.current();
    assertThat(sql).hasSize(2); // update of collection only
    assertThat(sql.get(0)).contains("delete from ecbl_person_phone_numbers where person_id=?");
    assertThat(sql.get(1)).contains("insert into ecbl_person_phone_numbers (person_id,country_code,area,number) values (?,?,?,?)");

    EcblPerson three = Ebean.find(EcblPerson.class)
      .setId(person.getId())
      .findOne();

    assertThat(three.getPhoneNumbers().toString()).contains("61-07-11", "64-021-1234");
    assertThat(three.getPhoneNumbers()).hasSize(2);

    sql = LoggedSqlCollector.current();
    assertThat(sql).isEmpty(); // cache hit


    three.setName("mod-3");
    three.getPhoneNumbers().remove(0);

    Ebean.save(three);

    sql = LoggedSqlCollector.current();
    assertThat(sql).hasSize(3);

    EcblPerson four = Ebean.find(EcblPerson.class)
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
