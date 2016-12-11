package org.tests.basic;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import org.tests.model.basic.Car;
import org.tests.model.basic.Truck;
import org.tests.model.basic.Vehicle;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class TestInheritRef extends BaseTestCase {

  @Test
  public void testAssocOne() {

    Ebean.beginTransaction();
    try {
      Ebean.createUpdate(Vehicle.class, "delete from vehicle");

      Car c = new Car();
      c.setLicenseNumber("C6788");
      c.setDriver("CarDriver");
      Ebean.save(c);

      Truck t = new Truck();
      t.setLicenseNumber("T1098BBX");
      t.setCapacity(20D);
      Ebean.save(t);

      List<Vehicle> list = Ebean.find(Vehicle.class)
        .setAutoTune(false)
        .findList();

      Assert.assertTrue(!list.isEmpty());

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

      Assert.assertEquals(1, found);
      Assert.assertTrue(foundTruck.getCapacity() == 20D);

    } finally {
      Ebean.rollbackTransaction();
    }
  }
}
