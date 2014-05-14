package com.avaje.tests.query.other;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.tests.model.basic.Truck;
import com.avaje.tests.model.basic.Vehicle;

public class TestFindIdsWithInheritance extends BaseTestCase {

  @Test
  public void testQuery() {
    
    Truck truck = new Truck();
    truck.setLicenseNumber("TK123");
    
    Ebean.save(truck);
    
    List<Object> ids = Ebean.find(Vehicle.class).findIds();
    
    Assert.assertNotNull(ids);
    
    Ebean.delete(truck);
    
  }
  
}
