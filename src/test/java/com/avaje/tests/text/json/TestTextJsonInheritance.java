package com.avaje.tests.text.json;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.text.json.JsonContext;
import com.avaje.tests.model.basic.*;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

public class TestTextJsonInheritance extends BaseTestCase {

  @Test
  public void test() throws IOException {

    setupData();

    List<Vehicle> list = Ebean.find(Vehicle.class).setAutoTune(false).findList();

    Assert.assertEquals(2, list.size());

    JsonContext jsonContext = Ebean.json();
    String jsonString = jsonContext.toJson(list);

    List<Vehicle> rebuiltList = jsonContext.toList(Vehicle.class, jsonString);

    Assert.assertEquals(2, rebuiltList.size());

  }

  private void setupData() {

    Ebean.createUpdate(CarAccessory.class, "delete from CarAccessory").execute();
    Ebean.createUpdate(CarFuse.class, "delete from CarFuse").execute();
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
