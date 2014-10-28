package com.avaje.tests.model.basic.first;

import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;

public class TestFirstSecond extends BaseTestCase {

  @Test
  public void test() {

    First first = new First();
    first.setName("first");
    Ebean.save(first);

    Second second = new Second();
    second.setTitle("John");
    second.setFirst(first);
    Ebean.save(second);

    
    second.getFirst().setName("changed");
    Ebean.save(second);
    Ebean.save(first);

    
  }
}
