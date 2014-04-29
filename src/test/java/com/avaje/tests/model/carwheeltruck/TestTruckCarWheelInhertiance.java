package com.avaje.tests.model.carwheeltruck;

import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;

public class TestTruckCarWheelInhertiance extends BaseTestCase {

  @Test
  public void test() {

    TTruck truck = new TTruck();
    truck.setPlateNo("foo");

    TWheel wheel = new TWheel();
    wheel.setOwner(truck);

    Ebean.save(truck);

    // This save() works ok...

    // But if then is added one more wheel
    wheel = new TWheel();
    wheel.setOwner(truck);

    // And save() is called again
    Ebean.save(truck);

    // Then an exception is raised:
  }

}
