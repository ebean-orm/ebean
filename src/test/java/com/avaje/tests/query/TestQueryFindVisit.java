package com.avaje.tests.query;

import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.TestCase;

import org.junit.Assert;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.FetchConfig;
import com.avaje.ebean.Query;
import com.avaje.ebean.QueryResultVisitor;
import com.avaje.tests.model.basic.Customer;
import com.avaje.tests.model.basic.ResetBasicData;

public class TestQueryFindVisit extends TestCase {

    public void test() {
        
        ResetBasicData.reset();
        
        EbeanServer server = Ebean.getServer(null);
        
        Query<Customer> query = server.find(Customer.class)
            .setAutofetch(false)
            .fetch("contacts",new FetchConfig().query(2))
            .where().gt("id", 0)
            .orderBy("id")
            .setMaxRows(2);
     
        final AtomicInteger counter = new AtomicInteger(0);
        
        query.findVisit(new QueryResultVisitor<Customer>() {
            
            public boolean accept(Customer bean) {
                counter.incrementAndGet();
                return true;
            }
        });
        
        Assert.assertEquals(2, counter.get());
    }
}
