package org.querytest;

import org.example.domain.query.QAreaImpl;
import org.example.domain.query.QCityImpl;
import org.junit.Test;

public class TargetTest {

  @Test
  public void test_oneToMany() {
    
    new QAreaImpl()
      .cities.fetch()
      .findList();
  }

  @Test
  public void test_manyToOne() {

    new QCityImpl()
      .country.fetch()
      .findList();
  }
}
