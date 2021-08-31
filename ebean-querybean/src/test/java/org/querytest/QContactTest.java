package org.querytest;

import org.example.domain.query.QContact;
import org.junit.Test;

import java.time.ZonedDateTime;

public class QContactTest {

  @Test
  public void test_oneToManyMap() {
    new QContact()
      .others.fetch()
      .zoneDateTime.before(ZonedDateTime.now())
      .findList();
  }
}
