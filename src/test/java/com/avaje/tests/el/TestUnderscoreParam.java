package com.avaje.tests.el;

import java.util.List;

import junit.framework.TestCase;

import org.junit.Assert;

import com.avaje.ebean.Ebean;
import com.avaje.tests.model.basic.Customer;

public class TestUnderscoreParam extends TestCase {

    public void test() {
        
        List<Customer> list = Ebean.find(Customer.class)
            .where("name like :cust_name")
            .setParameter("cust_name", "Rob%")
            .findList();
        
        Assert.assertNotNull(list);
        
    }
    
}
