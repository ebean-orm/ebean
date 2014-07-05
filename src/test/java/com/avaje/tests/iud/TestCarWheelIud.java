package com.avaje.tests.iud;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.config.GlobalProperties;
import com.avaje.tests.model.carwheel.Car;
import com.avaje.tests.model.carwheel.Tire;
import com.avaje.tests.model.carwheel.Wheel;

public class TestCarWheelIud extends BaseTestCase {

  @Test
	public void test() {

		GlobalProperties.put("ebean.search.packages", "com.avaje.tests.model.carwheel");
		
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
		
		List<Wheel> wheels = new ArrayList<Wheel>();
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
