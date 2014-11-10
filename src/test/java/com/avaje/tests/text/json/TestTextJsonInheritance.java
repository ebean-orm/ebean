package com.avaje.tests.text.json;

import java.io.IOException;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.text.json.JsonContext;
import com.avaje.tests.model.basic.Car;
import com.avaje.tests.model.basic.Trip;
import com.avaje.tests.model.basic.Truck;
import com.avaje.tests.model.basic.Vehicle;
import com.avaje.tests.model.basic.VehicleDriver;

public class TestTextJsonInheritance extends BaseTestCase {

  @Test
  public void test() throws IOException {

    setupData();

    List<Vehicle> list = Ebean.find(Vehicle.class).setAutofetch(false).findList();

    Assert.assertEquals(2, list.size());

    JsonContext jsonContext = Ebean.createJsonContext();
    String jsonString = jsonContext.toJsonString(list);
    System.out.println(jsonString);

    List<Vehicle> rebuiltList = jsonContext.toList(Vehicle.class, jsonString);

    Assert.assertEquals(2, rebuiltList.size());

  }

  private void setupData() {

    Ebean.createUpdate(Trip.class, "delete from trip").execute();
    Ebean.createUpdate(VehicleDriver.class, "delete from vehicleDriver").execute();
    Ebean.createUpdate(Vehicle.class, "delete from vehicle").execute();

    Car c = new Car();
    c.setLicenseNumber("C6788");
    c.setDriver("CarDriver");
    Ebean.save(c);

    Truck t = new Truck();
    t.setLicenseNumber("T1098");
    t.setCapacity(20D);
    Ebean.save(t);

  }
}
