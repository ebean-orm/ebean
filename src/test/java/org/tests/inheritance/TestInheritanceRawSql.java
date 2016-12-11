package org.tests.inheritance;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.RawSql;
import io.ebean.RawSqlBuilder;
import org.tests.model.basic.Truck;
import org.tests.model.basic.Vehicle;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class TestInheritanceRawSql extends BaseTestCase {

  @Test
  public void test() {

    Truck truck = new Truck();
    truck.setCapacity(50D);
    truck.setLicenseNumber("ASB23");

    Ebean.save(truck);


    String sql = "select dtype, id, license_number from vehicle where id = :id";
    RawSqlBuilder rawSqlBuilder = RawSqlBuilder.parse(sql);

    RawSql rawSql = rawSqlBuilder.create();

    List<Vehicle> list = Ebean.find(Vehicle.class)
      .setRawSql(rawSql)
      .setParameter("id", truck.getId())
      .findList();

    Assert.assertEquals(1, list.size());

    Vehicle vehicle2 = list.get(0);
    Assert.assertTrue(vehicle2 instanceof Truck);

    Truck truck2 = (Truck) vehicle2;
    Assert.assertEquals("ASB23", truck2.getLicenseNumber());

    // invoke lazy loading and set the capacity
    truck2.setCapacity(30D);

    // and now save
    Ebean.save(truck2);
  }

}
