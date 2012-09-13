package com.avaje.tests.query;

import junit.framework.TestCase;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Query;
import com.avaje.ebean.Query.UseIndex;
import com.avaje.tests.model.basic.Customer;
import com.avaje.tests.model.basic.ResetBasicData;

public class TestExprNestedDisjunction extends TestCase {

    public void test() {
        
        ResetBasicData.reset();
        
//      java.sql.Date dateAfter = java.sql.Date.valueOf("2010-01-01");

        java.sql.Date onAfter = java.sql.Date.valueOf("2009-08-31");

        Query<Customer> q = Ebean.find(Customer.class)
            .setUseIndex(UseIndex.NO)
                .where()
                  .disjunction()
                    .conjunction()
                      .startsWith("name", "r")
                      .eq("anniversary", onAfter)
                      .endJunction()
                    .conjunction()
                      .eq("status", Customer.Status.ACTIVE)
                      .gt("id", 0)
                      .endJunction()
                .orderBy().asc("name");
        
        q.findList();
        String s = q.getGeneratedSql();

        assertTrue(s.contains("(t0.name like ?  and t0.anniversary = ? )  or (t0.status = ?  and t0.id > ? )"));        
    }
    
}
