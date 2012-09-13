package com.avaje.tests.query.orderby;

import junit.framework.TestCase;

import org.junit.Assert;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Query;
import com.avaje.tests.model.basic.Customer;
import com.avaje.tests.model.basic.ResetBasicData;

public class TestOrderByWithFunction extends TestCase {

    public void testWithFunction() {
        
        ResetBasicData.reset();
        
        Query<Customer> query = Ebean.find(Customer.class)
            .order("length(name),name");
        
        query.findList();
        String sql = query.getGeneratedSql();
        
        Assert.assertTrue(sql.indexOf("order by length(t0.name)") > -1);
        
        
        String oq = "find customer ORDER BY LENGTH(name),name";
        query = Ebean.createQuery(Customer.class, oq);
        query.findList();
        
        sql = query.getGeneratedSql();
        Assert.assertTrue(sql.indexOf("order by LENGTH(t0.name)") > -1);
    }
}
