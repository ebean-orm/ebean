package org.tests.inheritance;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.Query;
import org.tests.model.basic.Car;
import org.tests.model.basic.CarAccessory;
import org.tests.model.basic.CarFuse;
import org.tests.model.basic.Truck;
import org.tests.model.basic.Vehicle;
import org.tests.model.basic.VehicleDriver;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.StrictAssertions.assertThat;
import static org.junit.Assert.*;

public class TestInheritInsert extends BaseTestCase {

  @Test
  public void testCasting() {

    Truck t = new Truck();
    t.setCapacity(10d);
    Ebean.save(t);

    Vehicle v = Ebean.find(Vehicle.class, t.getId());
    if (v instanceof Truck) {
      Truck t0 = (Truck) v;
      assertEquals(Double.valueOf(10d), t0.getCapacity());
      assertEquals(Double.valueOf(10d), ((Truck) v).getCapacity());
      assertNotNull(t0.getId());
    } else {
      assertTrue("v not a Truck?", false);
    }

    VehicleDriver driver = new VehicleDriver();
    driver.setName("Jim");
    driver.setVehicle(v);

    Ebean.save(driver);

    VehicleDriver d1 = Ebean.find(VehicleDriver.class, driver.getId());
    v = d1.getVehicle();
    if (v instanceof Truck) {
      Double capacity = ((Truck) v).getCapacity();
      assertEquals(Double.valueOf(10d), capacity);
      assertNotNull(v.getId());
    } else {
      assertTrue("v not a Truck?", false);
    }

    List<VehicleDriver> list = Ebean.find(VehicleDriver.class).findList();
    for (VehicleDriver vehicleDriver : list) {
      if (vehicleDriver.getVehicle() instanceof Truck) {
        Double capacity = ((Truck) vehicleDriver.getVehicle()).getCapacity();
        assertEquals(Double.valueOf(10d), capacity);
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

    assertNotNull(drivers);
    assertEquals(1, drivers.size());
    assertNotNull(drivers.get(0));

    assertEquals("Mario", drivers.get(0).getName());
    assertEquals("MARIOS_CAR_LICENSE", drivers.get(0).getVehicle().getLicenseNumber());

    Vehicle car2 = Ebean.find(Vehicle.class, car.getId());

    car2.setLicenseNumber("test");
    Ebean.save(car);

  }

  @Test
  public void test_AtOrderBy_on_ChildOfChild() {

    Car car = new Car();
    car.setLicenseNumber("ABC");
    Ebean.save(car);

    CarFuse fuse = new CarFuse();
    fuse.setLocationCode("xdfg");
    Ebean.save(fuse);

    CarAccessory accessory = new CarAccessory(car, fuse);
    Ebean.save(accessory);


    Query<Car> query = Ebean.find(Car.class)
      .fetch("accessories")
      .where()
      .eq("id", car.getId())
      .query();

    Car result = query.findOne();

    assertThat(query.getGeneratedSql()).contains("order by t0.id, t2.location_code");
    assertThat(query.getGeneratedSql()).contains("left join car_fuse t2 on t2.id = t1.fuse_id");

    assertNotNull(result);
  }

}
