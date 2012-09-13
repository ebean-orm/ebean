package com.avaje.tests.query;

import java.util.List;

import junit.framework.TestCase;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Query;
import com.avaje.tests.model.basic.Customer;
import com.avaje.tests.model.basic.ResetBasicData;

public class TestLimitAlterFetchMany extends TestCase {

    public void test() {
        
        ResetBasicData.reset();
        
        // Due to the use of maxRows... we will convert
        // the fetch join to contacts over to a query join
        // ... otherwise we wouldn't be able to use the 
        // limit offset clause
        
        Query<Customer> query = Ebean.find(Customer.class)
            // this will automatically get converted to a
            // query join ... due to the maxRows
            .fetch("contacts")
            .setMaxRows(5);
        
        List<Customer> list = query.findList();
        
        System.out.println(list);
        
    }
    
}
