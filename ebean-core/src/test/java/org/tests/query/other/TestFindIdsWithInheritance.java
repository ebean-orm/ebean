package org.tests.query.other;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import org.tests.model.basic.Truck;
import org.tests.model.basic.Vehicle;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class TestFindIdsWithInheritance extends BaseTestCase {

  @Test
  public void testQuery() {

    Truck truck = new Truck();
    truck.setLicenseNumber("TK123");

    Ebean.save(truck);

    List<Integer> ids = Ebean.find(Vehicle.class).findIds();
    Assert.assertNotNull(ids);

    Ebean.delete(truck);

  }

}
