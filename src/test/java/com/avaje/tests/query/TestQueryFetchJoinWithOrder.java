package com.avaje.tests.query;

import java.util.List;

import junit.framework.TestCase;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.FetchConfig;
import com.avaje.tests.model.basic.Customer;
import com.avaje.tests.model.basic.Order;
import com.avaje.tests.model.basic.ResetBasicData;

public class TestQueryFetchJoinWithOrder extends TestCase {

    public void test() {
        
        ResetBasicData.reset();
        
        List<Order> list = Ebean.find(Order.class)
            .fetch("details", new FetchConfig().query())
            .order().asc("id")
            .order().desc("details.id")
            .findList();
            
        assertNotNull(list);

        List<Order> list2 = Ebean.find(Order.class)
            .fetch("customer", new FetchConfig().query())
            .fetch("customer.contacts")
            .order().asc("id")
            .order().asc("customer.contacts.lastName")
            .findList();

        assertNotNull(list2);

        
        List<Customer> list3 = Ebean.find(Customer.class)
            .fetch("orders")
            .filterMany("orders").eq("status", Order.Status.NEW)
            .order().desc("orders.id")
            .findList();
        
        assertNotNull(list3);

    }
}
