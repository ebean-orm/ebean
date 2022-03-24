package org.tests.inheritance;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import io.ebean.Query;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TestInheritInsert extends BaseTestCase {

  @Test
  public void testCasting() {

    Truck t = new Truck();
    t.setCapacity(10d);
    DB.save(t);

    Vehicle v = DB.find(Vehicle.class, t.getId());
    if (v instanceof Truck) {
      Truck t0 = (Truck) v;
      assertEquals(Double.valueOf(10d), t0.getCapacity());
      assertEquals(Double.valueOf(10d), ((Truck) v).getCapacity());
      assertNotNull(t0.getId());
    } else {
      fail();
    }

    VehicleDriver driver = new VehicleDriver();
    driver.setName("Jim");
    driver.setVehicle(v);

    DB.save(driver);

    VehicleDriver d1 = DB.find(VehicleDriver.class, driver.getId());
    v = d1.getVehicle();
    if (v instanceof Truck) {
      Double capacity = ((Truck) v).getCapacity();
      assertEquals(Double.valueOf(10d), capacity);
      assertNotNull(v.getId());
    } else {
      fail("v not a Truck?");
    }

    List<VehicleDriver> list = DB.find(VehicleDriver.class).findList();
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
    DB.save(car);

    VehicleDriver driver = new VehicleDriver();
    driver.setName("Mario");
    driver.setVehicle(car);
    DB.save(driver);

    Query<VehicleDriver> query = DB.find(VehicleDriver.class);
    query.where().eq("vehicle.licenseNumber", "MARIOS_CAR_LICENSE");
    List<VehicleDriver> drivers = query.findList();

    assertNotNull(drivers);
    assertEquals(1, drivers.size());
    assertNotNull(drivers.get(0));

    assertEquals("Mario", drivers.get(0).getName());
    assertEquals("MARIOS_CAR_LICENSE", drivers.get(0).getVehicle().getLicenseNumber());

    Vehicle car2 = DB.find(Vehicle.class, car.getId());

    car2.setLicenseNumber("test");
    DB.save(car);

  }

  @Test
  public void test_AtOrderBy_on_ChildOfChild() {

    Car car = new Car();
    car.setLicenseNumber("ABC");
    DB.save(car);

    CarFuse fuse = new CarFuse();
    fuse.setLocationCode("xdfg");
    DB.save(fuse);

    CarAccessory accessory = new CarAccessory(car, fuse);
    DB.save(accessory);


    Query<Car> query = DB.find(Car.class)
      .fetch("accessories")
      .where()
      .eq("id", car.getId())
      .query();

    Car result = query.findOne();

    assertSql(query).contains("order by t0.id, t2.location_code");
    assertSql(query).contains("left join car_fuse t2 on t2.id = t1.fuse_id");

    assertNotNull(result);
  }

}
