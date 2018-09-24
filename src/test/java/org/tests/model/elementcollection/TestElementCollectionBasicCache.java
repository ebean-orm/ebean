package org.tests.model.elementcollection;

import io.ebean.Ebean;
import org.ebeantest.LoggedSqlCollector;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestElementCollectionBasicCache {

  @Test
  public void test() {

    EcPerson person = new EcPerson("Cache1");
    person.getPhoneNumbers().add("021 1234");
    person.getPhoneNumbers().add("021 4321");
    Ebean.save(person);

    EcPerson one = Ebean.find(EcPerson.class)
      .setId(person.getId())
      .findOne();

    one.getPhoneNumbers().size();

    LoggedSqlCollector.start();

    EcPerson two = Ebean.find(EcPerson.class)
      .setId(person.getId())
      .findOne();

    two.setName("CacheMod");
    two.getPhoneNumbers().add("027 234234");

    List<String> sql = LoggedSqlCollector.current();
    assertThat(sql).isEmpty(); // cache hit containing phone numbers

    Ebean.save(two);

    sql = LoggedSqlCollector.current();
    assertThat(sql).hasSize(3);

    Ebean.save(two);

    sql = LoggedSqlCollector.current();
    assertThat(sql).isEmpty(); // no change

    EcPerson three = Ebean.find(EcPerson.class)
      .setId(person.getId())
      .findOne();

    assertThat(three.getName()).isEqualTo("CacheMod");
    assertThat(three.getPhoneNumbers()).contains("021 1234", "021 4321", "027 234234");

    sql = LoggedSqlCollector.current();
    assertThat(sql).isEmpty(); // cache hit

    three.getPhoneNumbers().add("09 6534");
    Ebean.save(three);

    sql = LoggedSqlCollector.current();
    assertThat(sql).hasSize(2); // cache hit

    EcPerson four = Ebean.find(EcPerson.class)
      .setId(person.getId())
      .fetch("phoneNumbers")
      .findOne();

    assertThat(four.getName()).isEqualTo("CacheMod");
    assertThat(four.getPhoneNumbers()).contains("021 1234", "021 4321", "027 234234", "09 6534");

    sql = LoggedSqlCollector.current();
    assertThat(sql).isEmpty(); // cache hit

    Ebean.delete(four);

    LoggedSqlCollector.stop();
  }
}
