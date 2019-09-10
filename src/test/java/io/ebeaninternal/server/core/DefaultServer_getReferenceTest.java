package io.ebeaninternal.server.core;


import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import org.junit.Test;
import org.tests.model.basic.Car;
import org.tests.model.basic.Customer;
import org.tests.model.basic.ResetBasicData;
import org.tests.model.basic.Vehicle;

import static org.assertj.core.api.Assertions.assertThat;

public class DefaultServer_getReferenceTest extends BaseTestCase {

  @Test
  public void getReference_noPC() {
    Ebean.getReference(Customer.class, 42);
  }

  @Test
  public void getReference_when_inPC_expect_getFromPC() {

    ResetBasicData.reset();

    Ebean.execute(() -> {
      Customer loaded = Ebean.find(Customer.class, 1);
      Customer reference = Ebean.getReference(Customer.class, 1);
      assertThat(loaded).isSameAs(reference);
    });
  }


  @Test
  public void inherit_getReference_when_noPC() {

    Car car = new Car();
    car.setDriver("TestForRef");
    Ebean.save(car);

    Vehicle reference = Ebean.getReference(Vehicle.class, car.getId());

    assertThat(reference).isInstanceOf(Car.class);
    assertThat(reference.getId()).isSameAs(car.getId());

    Ebean.delete(car);
  }


  @Test
  public void inherit_getReference_when_inPC() {

    Car car = new Car();
    car.setDriver("TestForRef");
    Ebean.save(car);

    Ebean.execute(() -> {
        Vehicle loaded = Ebean.find(Vehicle.class, car.getId());
        Vehicle reference = Ebean.getReference(Vehicle.class, car.getId());
        assertThat(reference).isSameAs(loaded);
      }
    );
    Ebean.delete(car);
  }
}
