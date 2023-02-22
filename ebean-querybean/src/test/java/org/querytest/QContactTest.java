package org.querytest;

import org.example.domain.query.QContact;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;

public class QContactTest {

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
