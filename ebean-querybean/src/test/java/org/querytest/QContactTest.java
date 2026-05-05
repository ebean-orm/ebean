package org.querytest;

import io.ebean.InTuples;
import org.example.domain.query.QContact;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;

public class QContactTest {

  @Test
  void findInTuples() {

    var c = QContact.alias();

    InTuples inTuples = InTuples.of(c.firstName, c.lastName)
      .add("Rob", "B")
      .add("Bob", "C")
      .add("Mob", "D");

    var query = new QContact()
      .firstName.isNotNull()
      .email.isNotNull()
      .others.filterMany(o -> o.something.gt(43))
      .inTuples(inTuples)
      .query();

    query.findList();
    assertThat(query.getGeneratedSql()).contains("(t0.first_name,t0.last_name) in ((?,?),(?,?),(?,?))");
  }

  @Test
  public void test_oneToManyMap() {
    findThem();
    findThem(ZonedDateTime.now());
  }

  void findThem() {
    new QContact()
      .others.fetch()
      .zoneDateTime.before(ZonedDateTime.now())
      .findList();
  }

  void findThem(ZonedDateTime dateTime) {
    new QContact()
      .others.fetch()
      .zoneDateTime.before(dateTime)
      .findList();
  }
}
