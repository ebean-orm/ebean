package com.avaje.tests.query;

import junit.framework.TestCase;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.FetchConfig;
import com.avaje.tests.model.basic.Customer;
import com.avaje.tests.model.basic.ResetBasicData;

public class TestThreeLevelQueryJoin extends TestCase {

    public void test() {
        
        ResetBasicData.reset();
        
        Ebean.find(Customer.class)
            .fetch("orders", new FetchConfig().query())
            .fetch("orders.details", new FetchConfig().query())
            .fetch("orders.shipments", new FetchConfig().query())
            .fetch("shippingAddress", new FetchConfig().query())
            .fetch("billingAddress", new FetchConfig().query())
            .findList();
        
        
    }
    
}
