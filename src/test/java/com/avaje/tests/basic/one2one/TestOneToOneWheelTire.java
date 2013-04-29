package com.avaje.tests.basic.one2one;

import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;

public class TestOneToOneWheelTire extends BaseTestCase {

  @Test
  public void test() {

    Wheel w = new Wheel();
    Tire t = new Tire();
    t.setWheel(w);
    w.setTire(t);

    Ebean.save(t);
  }

}
