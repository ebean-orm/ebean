package com.avaje.tests.basic.one2one;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import org.junit.Test;

public class TestOneToOneWheelTire extends BaseTestCase {

  @Test
  public void test() {

    Wheel w = new Wheel();
    Tire t = new Tire();
    t.setWheel(w);
    w.setTire(t);

    Ebean.save(t);

    Ebean.delete(t);
  }

}
