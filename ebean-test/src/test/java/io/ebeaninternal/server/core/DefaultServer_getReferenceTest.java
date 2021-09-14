package io.ebeaninternal.server.core;


import io.ebean.BaseTestCase;
import io.ebean.DB;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Car;
import org.tests.model.basic.Customer;
import org.tests.model.basic.ResetBasicData;
import org.tests.model.basic.Vehicle;

import static org.assertj.core.api.Assertions.assertThat;

public class DefaultServer_getReferenceTest extends BaseTestCase {

  @Test
  public void getReference_noPC() {
    DB.reference(Customer.class, 42);
  }

  @Test
  public void getReference_when_inPC_expect_getFromPC() {

    ResetBasicData.reset();

    DB.execute(() -> {
      Customer loaded = DB.find(Customer.class, 1);
      Customer reference = DB.reference(Customer.class, 1);
      assertThat(loaded).isSameAs(reference);
    });
  }


  @Test
  public void inherit_getReference_when_noPC() {

    Car car = new Car();
    car.setDriver("TestForRef");
    DB.save(car);

    Vehicle reference = DB.reference(Vehicle.class, car.getId());

    assertThat(reference).isInstanceOf(Car.class);
    assertThat(reference.getId()).isSameAs(car.getId());

    DB.delete(car);
  }


  @Test
  public void inherit_getReference_when_inPC() {

    Car car = new Car();
    car.setDriver("TestForRef");
    DB.save(car);

    DB.execute(() -> {
        Vehicle loaded = DB.find(Vehicle.class, car.getId());
        Vehicle reference = DB.reference(Vehicle.class, car.getId());
        assertThat(reference).isSameAs(loaded);
      }
    );
    DB.delete(car);
  }
}
