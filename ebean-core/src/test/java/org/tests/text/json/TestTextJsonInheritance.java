package org.tests.text.json;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.text.json.JsonContext;
import org.tests.model.basic.Car;
import org.tests.model.basic.CarAccessory;
import org.tests.model.basic.CarFuse;
import org.tests.model.basic.Trip;
import org.tests.model.basic.Truck;
import org.tests.model.basic.Vehicle;
import org.tests.model.basic.VehicleDriver;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestTextJsonInheritance extends BaseTestCase {

  @Test
  public void parseJson_when_inheritanceType_outOfOrderDtype() {

    String fom = "{\"id\":90,\"name\":\"Frank\",\"vehicle\":{\"id\":42,\"licenseNumber\":\"T100\",\"capacity\":99.0,\"dtype\":\"T\"}}";

    VehicleDriver driver1 = DB.json().toBean(VehicleDriver.class, fom);
    assertThat(driver1.getVehicle()).isInstanceOf(Truck.class);
    assertThat(driver1.getVehicle().getLicenseNumber()).isEqualTo("T100");
  }

  @Test
  public void test() {

    setupData();

    List<Vehicle> list = DB.find(Vehicle.class).setAutoTune(false).findList();

    assertEquals(2, list.size());

    JsonContext jsonContext = DB.json();
    String jsonString = jsonContext.toJson(list);

    List<Vehicle> rebuiltList = jsonContext.toList(Vehicle.class, jsonString);

    assertEquals(2, rebuiltList.size());
  }

  private void setupData() {
    DB.createUpdate(CarAccessory.class, "delete from CarAccessory").execute();
    DB.createUpdate(CarFuse.class, "delete from CarFuse").execute();
    DB.createUpdate(Trip.class, "delete from trip").execute();
    DB.createUpdate(VehicleDriver.class, "delete from vehicleDriver").execute();
    DB.createUpdate(Vehicle.class, "delete from vehicle").execute();

    Car c = new Car();
    c.setLicenseNumber("C6788");
    c.setDriver("CarDriver");
    DB.save(c);

    Truck t = new Truck();
    t.setLicenseNumber("T1098");
    t.setCapacity(20D);
    DB.save(t);
  }
}
