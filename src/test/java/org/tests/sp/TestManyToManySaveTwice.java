package org.tests.sp;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import org.tests.sp.model.car.Car;
import org.tests.sp.model.car.Wheel;
import org.junit.Assert;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

public class TestManyToManySaveTwice extends BaseTestCase {

  @Test
  public void testInsertCarTwice() {

    Ebean.createSqlUpdate("delete from sp_car_car_wheels").execute();
    Ebean.createSqlUpdate("delete from sp_car_wheel").execute();
    Ebean.createSqlUpdate("delete from sp_car_car").execute();

    List<Wheel> wheels = new LinkedList<>();
    wheels.add(new Wheel());
    wheels.add(new Wheel());
    wheels.add(new Wheel());
    wheels.add(new Wheel());

    Car c = new Car();
    c.setWheels(wheels);

    Ebean.save(c); // NOTE 1ST SAVE

    Assert.assertFalse("No ID assigned!", c.getId() == null);

    Ebean.save(c); // NOTE 2ND SAVE

    List<Car> allCars = Ebean.find(Car.class).findList();
    Assert.assertEquals("Inserted 1 car, received more/less!", 1, allCars.size());

    List<Wheel> allWheels = Ebean.find(Wheel.class).findList();
    Assert.assertEquals("Inserted 4 wheels, received more/less!", 4, allWheels.size());
  }
}
