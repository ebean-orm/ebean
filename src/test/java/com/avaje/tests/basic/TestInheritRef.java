package com.avaje.tests.basic;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.tests.model.basic.Car;
import com.avaje.tests.model.basic.Truck;
import com.avaje.tests.model.basic.Vehicle;

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
              .setAutofetch(false)
              .findList();

      Assert.assertTrue(list.size() > 0);

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
