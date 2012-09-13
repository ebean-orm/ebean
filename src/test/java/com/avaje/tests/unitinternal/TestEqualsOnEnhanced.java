package com.avaje.tests.unitinternal;

import junit.framework.TestCase;

import com.avaje.ebean.bean.EntityBean;
import com.avaje.tests.model.basic.Customer;
import com.avaje.tests.model.basic.Order;

public class TestEqualsOnEnhanced extends TestCase {

    public void test() {
        
        Customer c = new Customer();
        if (c instanceof EntityBean){
            Order o = new Order();
            if (o instanceof EntityBean){
                
                c.setId(1);
                
                o.setId(1);
                
                assertFalse(c.equals(o));
                assertFalse(c.equals(null));
                assertTrue(c.equals(c));
                
                Customer c2 = new Customer();
                c2.setId(1);
                assertTrue(c.equals(c2));

                Customer c3 = new Customer();
                //c2.setId(1);
                assertFalse(c.equals(c3));
                
                Customer c4 = new Customer();
                c4.setId(2);
                assertFalse(c.equals(c4));

            }
        }
        
        
    }
}
