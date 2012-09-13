package com.avaje.tests.query;

import junit.framework.TestCase;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Query;
import com.avaje.tests.model.basic.Order;

public class TestNoSpaceBracket extends TestCase {

    
    public void test() {
        
        Query<Order> query = Ebean.createQuery(Order.class, "find order join customer(id,name) ");
        
        query.findList();
        
    }
    
}
