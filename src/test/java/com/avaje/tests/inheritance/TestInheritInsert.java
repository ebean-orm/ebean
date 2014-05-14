package com.avaje.tests.inheritance;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.Query;
import com.avaje.tests.model.basic.Car;
import com.avaje.tests.model.basic.Truck;
import com.avaje.tests.model.basic.Vehicle;
import com.avaje.tests.model.basic.VehicleDriver;

public class TestInheritInsert extends BaseTestCase {

  @Test
  public void testCasting() {

    Truck t = new Truck();
    t.setCapacity(10d);
    Ebean.save(t);

    Vehicle v = Ebean.find(Vehicle.class, t.getId());
    if (v instanceof Truck) {
      Truck t0 = (Truck) v;
      Assert.assertEquals(Double.valueOf(10d), t0.getCapacity());
      Assert.assertEquals(Double.valueOf(10d), ((Truck) v).getCapacity());
      Assert.assertNotNull(t0.getId());
    } else {
      Assert.assertTrue("v not a Truck?", false);
    }

    VehicleDriver driver = new VehicleDriver();
    driver.setName("Jim");
    driver.setVehicle(v);

    Ebean.save(driver);

    VehicleDriver d1 = Ebean.find(VehicleDriver.class, driver.getId());
    v = d1.getVehicle();
    if (v instanceof Truck) {
      Double capacity = ((Truck) v).getCapacity();
      Assert.assertEquals(Double.valueOf(10d), capacity);
      Assert.assertNotNull(v.getId());
    } else {
      Assert.assertTrue("v not a Truck?", false);
    }

    List<VehicleDriver> list = Ebean.find(VehicleDriver.class).findList();
    for (VehicleDriver vehicleDriver : list) {
      if (vehicleDriver.getVehicle() instanceof Truck) {
        Double capacity = ((Truck) vehicleDriver.getVehicle()).getCapacity();
        Assert.assertEquals(Double.valueOf(10d), capacity);
      }
    }
  }

  @Test
  public void testQuery() {

    Car car = new Car();
    car.setLicenseNumber("MARIOS_CAR_LICENSE");
    Ebean.save(car);

    VehicleDriver driver = new VehicleDriver();
    driver.setName("Mario");
    driver.setVehicle(car);
    Ebean.save(driver);

    Query<VehicleDriver> query = Ebean.find(VehicleDriver.class);
    query.where().eq("vehicle.licenseNumber", "MARIOS_CAR_LICENSE");
    List<VehicleDriver> drivers = query.findList();

    Assert.assertNotNull(drivers);
    Assert.assertEquals(1, drivers.size());
    Assert.assertNotNull(drivers.get(0));

    Assert.assertEquals("Mario", drivers.get(0).getName());
    Assert.assertEquals("MARIOS_CAR_LICENSE", drivers.get(0).getVehicle().getLicenseNumber());

    Vehicle car2 = Ebean.find(Vehicle.class, car.getId());

    car2.setLicenseNumber("test");
    Ebean.save(car);

  }
}
