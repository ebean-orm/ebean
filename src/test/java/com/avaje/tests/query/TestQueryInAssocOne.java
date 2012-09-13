package com.avaje.tests.query;

import java.util.List;

import junit.framework.TestCase;

import org.junit.Assert;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Query;
import com.avaje.tests.model.basic.Customer;
import com.avaje.tests.model.basic.Order;
import com.avaje.tests.model.basic.ResetBasicData;

public class TestQueryInAssocOne extends TestCase {

    
    public void test() {
        
        ResetBasicData.reset();
        
        List<Customer> list = Ebean.find(Customer.class)
            .where().lt("id",200)
            .findList();
        
        Query<Order> query = Ebean.find(Order.class)
            .where().in("customer", list)
            .query();
        
        query.findList();
        String sql = query.getGeneratedSql();
        
        Assert.assertTrue(sql, sql.indexOf("join o_customer t1 on t1.id = t0.kcustomer_id") > -1);
        Assert.assertTrue(sql, sql.indexOf("t0.kcustomer_id in (?") > -1);
        
    }
}
