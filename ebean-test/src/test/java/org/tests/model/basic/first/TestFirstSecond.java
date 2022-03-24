package org.tests.model.basic.first;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import org.junit.jupiter.api.Test;

public class TestFirstSecond extends BaseTestCase {

  @Test
  public void test() {

    First first = new First();
    first.setName("first");
    DB.save(first);

    Second second = new Second();
    second.setName("Jim");
    second.setTitle("Sir");
    second.setFirst(first);
    DB.save(second);


    second.getFirst().setName("changed");
    DB.save(second);
    DB.save(first);
  }
}
