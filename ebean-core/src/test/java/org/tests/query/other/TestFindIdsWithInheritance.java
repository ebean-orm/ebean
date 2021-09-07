package org.tests.query.other;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import org.tests.model.basic.Truck;
import org.tests.model.basic.Vehicle;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TestFindIdsWithInheritance extends BaseTestCase {

  @Test
  public void testQuery() {

    Truck truck = new Truck();
    truck.setLicenseNumber("TK123");

    DB.save(truck);

    List<Integer> ids = DB.find(Vehicle.class).findIds();
    assertNotNull(ids);

    DB.delete(truck);
  }

}
