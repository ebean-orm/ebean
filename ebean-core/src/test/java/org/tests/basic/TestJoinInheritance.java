package org.tests.basic;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import org.tests.model.basic.Address;
import org.tests.model.basic.Car;
import org.tests.model.basic.Trip;
import org.tests.model.basic.Vehicle;
import org.tests.model.basic.VehicleDriver;
import org.junit.Test;

import java.sql.Timestamp;
import java.util.Date;

public class TestJoinInheritance extends BaseTestCase {


  /**
   * Test join hierarchy assoc one.
   * <p>
   * This test catches the problem where the discriminator column is select
   * but is not skipped if the parent bean is already in the presistence context.
   * <p>
   * Test case: We have several trips with different addresses but always the same
   * driver and car (the cars could be different). So the driver will be loaded
   * when the first trip is loaded but when the second trip is loaded the driver is
   * found in the persistence context. So the bean is not read but the index in the
   * result set is forwarded by the number of properties - however, the vehicle type
   * column was not being skipped - but the query reads happily - only the data is
   * then wrong in the address object.
   */
  @Test
  public void testJoinHierarchyAssocOne() {

    Ebean.createUpdate(Vehicle.class, "delete from vehicle");


    Address address = new Address();
    address.setLine1("Street");
    address.setLine2("Street");
    address.setCity("City");

    address.setCretime(new Timestamp(new Date().getTime()));

    Ebean.save(address);

    Car c = new Car();
    c.setLicenseNumber("C6788");
    c.setDriver("CarDriver");
    c.setRegistrationDate(new Date());
    Ebean.save(c);

    VehicleDriver driver = new VehicleDriver();

    driver.setVehicle(c);
    driver.setAddress(address);
    driver.setLicenseIssuedOn(new Date());
    Ebean.save(driver);

    final String line1 = "Street1";
    final String line2 = "Street2";
    final String city = "City";

    int nrDrivers = 2;
    for (int i = 0; i < nrDrivers; i++) {
      Address address1 = new Address();
      address1.setLine1(line1);
      address1.setLine2(line2);
      address1.setCity(city);
      Ebean.save(address1);

      Trip trip = new Trip();
      trip.setVehicleDriver(driver);
      trip.setAddress(address1);
      trip.setCretime(new Timestamp(new Date().getTime()));
      Ebean.save(trip);
    }

  }
}
