package com.avaje.tests.inheritance;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.RawSql;
import com.avaje.ebean.RawSqlBuilder;
import com.avaje.tests.model.basic.Truck;
import com.avaje.tests.model.basic.Vehicle;

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

    Truck truck2 = (Truck)vehicle2;
    Assert.assertEquals("ASB23", truck2.getLicenseNumber());
    
    // invoke lazy loading and set the capacity
    truck2.setCapacity(30D);
    
    // and now save
    Ebean.save(truck2);
  }
  
}
