package org.tests.sp;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.Ebean;
import io.ebean.Transaction;
import org.junit.Test;
import org.tests.sp.model.car.Car;
import org.tests.sp.model.car.Wheel;

import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TestManyToManySaveTwice extends BaseTestCase {

  @Test
  public void insertBatch() {

    delete();

    Wheel w0 = new Wheel("wx0");
    Wheel w1 = new Wheel("wx1");

    List<Wheel> wheels = new LinkedList<>();
    wheels.add(w0);
    wheels.add(w1);

    DB.saveAll(wheels);

    Car c0 = new Car("cx0");
    c0.getWheels().add(w0);
    c0.getWheels().add(w1);

    Car c1 = new Car("cx1");
    c1.getWheels().add(w1);

    try (Transaction transaction = DB.beginTransaction()) {
      transaction.setBatchMode(true);

      DB.save(c0);
      DB.save(c1);

      transaction.commit();
    }
  }


  @Test
  public void insertBasic() {

    delete();

    Wheel w0 = new Wheel("wx0");
    Wheel w1 = new Wheel("wx1");

    List<Wheel> wheels = new LinkedList<>();
    wheels.add(w0);
    wheels.add(w1);

    DB.saveAll(wheels);

    Car c0 = new Car("cx0");
    c0.getWheels().add(w0);
    c0.getWheels().add(w1);

    Car c1 = new Car("cx1");
    c1.getWheels().add(w1);

    DB.save(c0);
    DB.save(c1);
  }

  @Test
  public void testInsertCarTwice() {

    delete();

    List<Wheel> wheels = new LinkedList<>();
    wheels.add(new Wheel("w0"));
    wheels.add(new Wheel("w1"));
    wheels.add(new Wheel("w2"));
    wheels.add(new Wheel("w3"));

    Car c = new Car("c1");
    c.setWheels(wheels);

    DB.save(c); // NOTE 1ST SAVE
    assertNotNull(c.getId());

    DB.save(c); // NOTE 2ND SAVE

    List<Car> allCars = Ebean.find(Car.class).findList();
    assertEquals(1, allCars.size());

    List<Wheel> allWheels = Ebean.find(Wheel.class).findList();
    assertEquals(4, allWheels.size());
  }

  private void delete() {
    DB.sqlUpdate("delete from sp_car_car_wheels").execute();
    DB.sqlUpdate("delete from sp_car_wheel").execute();
    DB.sqlUpdate("delete from sp_car_car").execute();
  }
}
