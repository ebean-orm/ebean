package org.tests.model.basic.first;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import org.junit.Test;

public class TestFirstSecond extends BaseTestCase {

  @Test
  public void test() {

    First first = new First();
    first.setName("first");
    Ebean.save(first);

    Second second = new Second();
    second.setName("Jim");
    second.setTitle("Sir");
    second.setFirst(first);
    Ebean.save(second);


    second.getFirst().setName("changed");
    Ebean.save(second);
    Ebean.save(first);
  }
}
