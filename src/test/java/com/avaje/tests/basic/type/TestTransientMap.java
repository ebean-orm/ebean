package com.avaje.tests.basic.type;

import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.config.GlobalProperties;

public class TestTransientMap extends BaseTestCase {

  @Test
  public void testMe() {

    GlobalProperties.put("classes", BSimpleWithGen.class.toString());

    BSimpleWithGen b = new BSimpleWithGen();
    b.setName("blah");

    Ebean.save(b);

  }
}
