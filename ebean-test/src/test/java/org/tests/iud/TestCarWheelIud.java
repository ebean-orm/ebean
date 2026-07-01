package org.tests.iud;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import io.ebean.PagedList;
import io.ebean.test.LoggedSql;
import org.junit.jupiter.api.Test;
import org.tests.model.carwheel.Car;
import org.tests.model.carwheel.Tire;
import org.tests.model.carwheel.Wheel;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

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

    DB.save(car);

    // And I'm trying to delete this entry with code:
    Car car2 = DB.find(Car.class, car.getId());

    DB.delete(car2);
  }

  @Test
  public void aggregatePaging() {

    DB.find(Car.class).delete();

    final Car car0 = createCar("Ford", 40);
    final Car car1 = createCar("Ford", 50);
    final Car car2 = createCar("Mazda", 12);

    DB.saveAll(asList(car0, car1, car2));

    LoggedSql.start();

    final PagedList<Car> pagedList = DB.find(Car.class)
      .select("brand, totalSold")
      .setMaxRows(10)
      .findPagedList();

    final List<Car> list = pagedList.getList();
    final int count = pagedList.getTotalCount();

    assertThat(list).hasSize(2);
    assertThat(count).isEqualTo(2);

    final List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(2);
    if (isH2() || isPostgresCompatible()) {
      assertSql(sql.get(0)).contains("select t0.brand, sum(t0.sold) from sa_car t0 group by t0.brand limit 10");
      assertSql(sql.get(1)).contains("select count(*) from ( select t0.brand, sum(t0.sold) from sa_car t0 group by t0.brand)");
    }

  }

  private Car createCar(String brand, int sold) {
    Car car0 = new Car();
    car0.setBrand(brand);
    car0.setSold(sold);
    return car0;
  }
}
