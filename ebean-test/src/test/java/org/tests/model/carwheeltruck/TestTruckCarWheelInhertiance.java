package org.tests.model.carwheeltruck;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import org.junit.jupiter.api.Test;

import java.util.Random;

public class TestTruckCarWheelInhertiance extends BaseTestCase {

  @Test
  public void test() {

    TTruck truck = new TTruck();
    truck.setPlateNo("foo-" + new Random().nextInt());

    TWheel wheel = new TWheel();
    wheel.setOwner(truck);

    DB.save(truck);

    // This save() works ok...

    // But if then is added one more wheel
    wheel = new TWheel();
    wheel.setOwner(truck);

    // And save() is called again
    DB.save(truck);

    // Then an exception is raised:
  }

}
