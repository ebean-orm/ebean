package com.avaje.tests.basic;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import com.avaje.ebean.Ebean;
import com.avaje.tests.model.basic.Customer;
import com.avaje.tests.model.basic.Order;
import com.avaje.tests.model.basic.ResetBasicData;

public class TestDeleteByIdCollection extends TestCase {

    public void test() {
        
        ResetBasicData.reset();
        
        Customer c0 = ResetBasicData.createCustomer("del1", "del1 ship", "del1 bill", 1);
        Customer c1 = ResetBasicData.createCustomer("del2", "del2 ship", "del2 bill", 2);
        
        Ebean.save(c0);
        Ebean.save(c1);

        Customer c0Back = Ebean.find(Customer.class, c0.getId());
        Customer c1Back = Ebean.find(Customer.class, ""+c1.getId());

        assertNotNull(c0Back);
        assertNotNull(c1Back);
        
        List<String> ids = new ArrayList<String>();
        // also test id type conversion
        ids.add(""+c0.getId());
        ids.add(""+c1.getId());
        
        
        Ebean.delete(Customer.class, ids);
        
        c0Back = Ebean.find(Customer.class, c0.getId());
        c1Back = Ebean.find(Customer.class, ""+c1.getId());
        
        assertNull(c0Back);
        assertNull(c1Back);
    }
    
    public void testDelByStatement() {

        ResetBasicData.reset();
        
        Order order0 = ResetBasicData.createOrderCustAndOrder("delBySql 0");
        Order order1 = ResetBasicData.createOrderCustAndOrder("delBySql 1");
        
        Order o0Back = Ebean.find(Order.class, order0.getId());
        Order o1Back = Ebean.find(Order.class, order1.getId());
        
        assertNotNull(o0Back);
        assertNotNull(o1Back);
        

        List<Object> ids = new ArrayList<Object>();
        // also test id type conversion
        ids.add(order0.getId());
        ids.add(order1.getId());

        Ebean.delete(Order.class, ids);
        
        o0Back = Ebean.find(Order.class, order0.getId());
        o1Back = Ebean.find(Order.class, order1.getId());
        
        assertNull(o0Back);
        assertNull(o1Back);
    }
    
}
