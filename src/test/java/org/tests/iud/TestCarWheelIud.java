package org.tests.iud;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import org.tests.model.carwheel.Car;
import org.tests.model.carwheel.Tire;
import org.tests.model.carwheel.Wheel;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class TestCarWheelIud extends BaseTestCase {

  @Test
  public void test() {

    Car car = new Car();

    Tire t1 = new Tire();
    Wheel w1 = new Wheel();
    w1.setCar(car);
    w1.setTire(t1);

    Tire t2 = new Tire();
    Wheel w2 = new Wheel();
    w2.setCar(car);
    w2.setTire(t2);

    Tire t3 = new Tire();
    Wheel w3 = new Wheel();
    w3.setCar(car);
    w3.setTire(t3);

    Tire t4 = new Tire();
    Wheel w4 = new Wheel();
    w4.setCar(car);
    w4.setTire(t4);

    List<Wheel> wheels = new ArrayList<>();
    wheels.add(w1);
    wheels.add(w2);
    wheels.add(w3);
    wheels.add(w4);

    car.setWheels(wheels);

    Ebean.save(car);

    // And I'm trying to delete this entry with code:
    Car car2 = Ebean.find(Car.class, car.getId());

    Ebean.delete(car2);

  }
}
