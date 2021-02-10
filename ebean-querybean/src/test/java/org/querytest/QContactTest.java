package org.querytest;

import org.example.domain.query.QContact;
import org.junit.Test;

public class QContactTest {

  @Test
  public void test_oneToManyMap() {

    new QContact()
      .others.fetch()
      .findList();
  }
}
