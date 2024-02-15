package org.tests.basic;

import io.ebean.Transaction;
import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Car;
import org.tests.model.basic.Truck;
import org.tests.model.basic.Vehicle;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestInheritRef extends BaseTestCase {

  @Test
  public void testAssocOne() {

    try (Transaction txn = DB.beginTransaction()) {
      DB.createUpdate(Vehicle.class, "delete from vehicle");

      Car c = new Car();
      c.setLicenseNumber("C6788");
      c.setDriver("CarDriver");
      DB.save(c);

      Truck t = new Truck();
      t.setLicenseNumber("T1098BBX");
      t.setCapacity(20D);
      DB.save(t);

      List<Vehicle> list = DB.find(Vehicle.class)
        .setAutoTune(false)
        .findList();

      assertTrue(!list.isEmpty());

      Truck foundTruck = null;
      int found = 0;

      for (Vehicle vehicle : list) {
        if (vehicle instanceof Truck) {
          Truck truck = (Truck) vehicle;
          if ("T1098BBX".equals(truck.getLicenseNumber())) {
            found++;
            foundTruck = truck;
          }
        }
      }

      assertEquals(1, found);
      assertTrue(foundTruck.getCapacity() == 20D);
    }
  }
}
