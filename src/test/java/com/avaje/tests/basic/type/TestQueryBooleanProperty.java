package com.avaje.tests.basic.type;

import junit.framework.TestCase;

import com.avaje.ebean.Ebean;
import com.avaje.tests.model.basic.TOne;

public class TestQueryBooleanProperty extends TestCase {

    public void test() {
        
        // when run in MySql is test for BUG 323
        Ebean.find(TOne.class)
            .where().eq("active", true)
            .findList();
    }
}
