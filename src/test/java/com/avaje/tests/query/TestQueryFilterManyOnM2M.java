package com.avaje.tests.query;

import junit.framework.TestCase;

import com.avaje.ebean.Ebean;
import com.avaje.tests.model.basic.MUser;

public class TestQueryFilterManyOnM2M extends TestCase {

    public void test() {
        
        Ebean.find(MUser.class)
            .fetch("roles")
            .filterMany("roles").ilike("roleName","Jim%")
            .findList();
        
        
    }
    
}
