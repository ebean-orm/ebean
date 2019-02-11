package org.tests.sp;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.Ebean;
import io.ebean.Transaction;
import org.junit.Test;
import org.tests.sp.model.car.Car;
import org.tests.sp.model.car.Door;
import org.tests.sp.model.car.Wheel;

import java.util.LinkedList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TestManyToManySaveTwice extends BaseTestCase {

  @Test
  public void insertBatch() {

    delete();

    Wheel w0 = new Wheel("wx0");
    Wheel w1 = new Wheel("wx1");

    Door d0 = new Door("dx0");
    Door d1 = new Door("dx1");

    List<Wheel> wheels = new LinkedList<>();
    wheels.add(w0);
    wheels.add(w1);

    List<Door> doors = new LinkedList<>();
    doors.add(d0);
    doors.add(d1);

    DB.saveAll(wheels);
    DB.saveAll(doors);

    Car c0 = new Car("cx0");
    c0.getWheels().add(w0);
    c0.getWheels().add(w1);
    c0.getDoors().add(d0);

    Car c1 = new Car("cx1");
    c1.getWheels().add(w1);

    try (Transaction transaction = DB.beginTransaction()) {
      transaction.setBatchMode(true);

      DB.save(c0);
      DB.save(c1);

      transaction.commit();
    }

    c0 = Ebean.find(Car.class, c0.getId());
    c1 = Ebean.find(Car.class, c1.getId());

    assertThat(c0.getWheels()).hasSize(2);
    assertThat(c0.getDoors()).hasSize(1);

    assertThat(c1.getWheels()).hasSize(1);
    assertThat(c1.getDoors()).hasSize(0);

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
    DB.sqlUpdate("delete from sp_car_car_doors").execute();
    DB.sqlUpdate("delete from sp_car_wheel").execute();
    DB.sqlUpdate("delete from sp_car_door").execute();
    DB.sqlUpdate("delete from sp_car_car").execute();
  }
}
