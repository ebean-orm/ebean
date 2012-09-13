package com.avaje.tests.query.orderby;

import junit.framework.TestCase;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Query;
import com.avaje.tests.model.basic.Order;
import com.avaje.tests.model.basic.ResetBasicData;

public class TestOrderByOnComplex extends TestCase {

    public void test() {
        
        ResetBasicData.reset();
        
        Query<Order> query = Ebean.find(Order.class)
            .order().desc("customer");
        
        query.findList();
        
        String sql = query.getGeneratedSql();
        assertTrue(sql.contains("order by t0.kcustomer_id desc"));
            
    }
    
}
