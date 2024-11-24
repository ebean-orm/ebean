package org.tests.inheritance;

import io.ebean.DB;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Car;
import org.tests.model.basic.Truck;
import org.tests.model.basic.Vehicle;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


public class TestInheritanceBatchLazyLoad {

  @Test
  public void lazyLoadProperty_when_propertyNotOnAllInheritanceTypes() {

    Car c = new Car();
    c.setLicenseNumber("VZVZ1");
    c.setDriver("CarDriver");
    DB.save(c);

    Truck t = new Truck();
    t.setLicenseNumber("VZVZ2");
    t.setCapacity(20D);
    DB.save(t);


    List<Vehicle> list = DB.find(Vehicle.class)
      .select("licenseNumber")
      .where().startsWith("licenseNumber", "VZVZ")
      .orderBy().asc("licenseNumber")
      .findList();

    assertThat(list).hasSize(2);

    Car car = (Car) list.get(0);
    car.getNotes();
  }
}
