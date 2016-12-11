package org.tests.basic.type;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import org.junit.Test;

public class TestTransientMap extends BaseTestCase {

  @Test
  public void testMe() {

    BSimpleWithGen b = new BSimpleWithGen();
    b.setName("blah");

    Ebean.save(b);

  }
}
