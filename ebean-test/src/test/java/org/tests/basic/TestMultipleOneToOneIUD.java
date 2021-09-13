package org.tests.basic;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.Transaction;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.OCar;
import org.tests.model.basic.OEngine;
import org.tests.model.basic.OGearBox;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class TestMultipleOneToOneIUD extends BaseTestCase {

  @Test
  public void test() {

    OEngine engine = new OEngine();
    engine.setShortDesc("engine 1");

    OGearBox gearBox = new OGearBox();
    gearBox.setBoxDesc("6 speed manual");
    gearBox.setSize(6);

    OCar car = new OCar();
    car.setVin("xx4534");
    car.setName("test car");
    car.setEngine(engine);

    try (Transaction txn = DB.beginTransaction()) {
      DB.save(gearBox);
      DB.save(car);

      assertNotNull(car.getId());
      assertNotNull(engine.getEngineId());
      assertNotNull(gearBox.getId());
      txn.commit();
    }

    OCar c2 = DB.find(OCar.class, car.getId());
    assertNotNull(c2);
    assertNotNull(c2.getEngine());
    // gearBox not assigned yet
    assertNull(c2.getGearBox());

    // ok, assign gearBox
    c2.setGearBox(gearBox);
    DB.save(c2);

    // now all should be there...
    OCar c3 = DB.find(OCar.class, car.getId());
    assertNotNull(c3);
    assertNotNull(c3.getEngine());
    assertNotNull(c3.getGearBox());

  }
}
