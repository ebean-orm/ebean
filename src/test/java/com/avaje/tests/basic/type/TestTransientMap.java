package com.avaje.tests.basic.type;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import org.junit.Test;

public class TestTransientMap extends BaseTestCase {

  @Test
  public void testMe() {

    BSimpleWithGen b = new BSimpleWithGen();
    b.setName("blah");

    Ebean.save(b);

  }
}
