package org.tests.basic.one2one;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import org.junit.jupiter.api.Test;

public class TestOneToOneWheelTire extends BaseTestCase {

  @Test
  public void test() {

    Wheel w = new Wheel();
    Tire t = new Tire();
    t.setWheel(w);
    w.setTire(t);

    DB.save(t);

    DB.delete(t);
  }

}
