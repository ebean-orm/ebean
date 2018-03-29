package org.tests.model.elementcollection;

import io.ebean.Ebean;
import org.ebeantest.LoggedSqlCollector;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestElementCollectionBasicMapCache {

  @Test
  public void test() {

    EcmPerson person = new EcmPerson("CacheMap");
    person.getPhoneNumbers().put("home", "021 1234");
    person.getPhoneNumbers().put("work", "021 4321");
    Ebean.save(person);


    EcmPerson one = Ebean.find(EcmPerson.class)
      .setId(person.getId())
      .fetch("phoneNumbers")
      .findOne();

    one.getPhoneNumbers().size();

    LoggedSqlCollector.start();

    EcmPerson two = Ebean.find(EcmPerson.class)
      .setId(person.getId())
      .findOne();

    two.setName("CacheMod");
    two.getPhoneNumbers().put("mob", "027 234234");

    List<String> sql = LoggedSqlCollector.current();
    assertThat(sql).isEmpty(); // cache hit containing phone numbers

    Ebean.save(two);

    sql = LoggedSqlCollector.current();
    assertThat(sql).hasSize(3);

    Ebean.save(two);

    sql = LoggedSqlCollector.current();
    assertThat(sql).isEmpty(); // no change

    EcmPerson three = Ebean.find(EcmPerson.class)
      .setId(person.getId())
      .findOne();

    assertThat(three.getName()).isEqualTo("CacheMod");
    assertThat(three.getPhoneNumbers().toString()).contains("021 1234", "021 4321", "027 234234");

    sql = LoggedSqlCollector.current();
    assertThat(sql).isEmpty(); // cache hit

    three.getPhoneNumbers().put("oth", "09 6534");
    three.getPhoneNumbers().remove("home");
    three.getPhoneNumbers().remove("work");
    Ebean.save(three);

    sql = LoggedSqlCollector.current();
    assertThat(sql).hasSize(2); // cache hit

    EcmPerson four = Ebean.find(EcmPerson.class)
      .setId(person.getId())
      .fetch("phoneNumbers")
      .findOne();

    assertThat(four.getName()).isEqualTo("CacheMod");
    assertThat(four.getPhoneNumbers().toString()).contains("027 234234", "09 6534");
    assertThat(four.getPhoneNumbers()).hasSize(2);

    sql = LoggedSqlCollector.current();
    assertThat(sql).isEmpty(); // cache hit

    Ebean.delete(four);

    LoggedSqlCollector.stop();
  }
}
