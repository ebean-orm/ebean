package com.avaje.tests.query;

import java.util.List;

import junit.framework.TestCase;

import com.avaje.ebean.Ebean;
import com.avaje.tests.model.basic.Customer;
import com.avaje.tests.model.basic.ResetBasicData;

public class TestQueryPathJoinAndOrder extends TestCase {

    public void test() {
        
        ResetBasicData.reset();
        
        List<Customer> list = Ebean.find(Customer.class)
          .select("id,name, status")
          .fetch("contacts")
          .order().asc("id")
          .order().desc("contacts.firstName")
          .setMaxRows(3)
          .findList();
        
        assertNotNull(list);
        
        // can't really assert that the contacts are batch loaded
        // via a secondary query join
        
    }
}
