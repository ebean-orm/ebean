package com.avaje.tests.text.json;

import junit.framework.TestCase;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.text.json.JsonContext;
import com.avaje.ebeaninternal.server.lib.util.StringHelper;
import com.avaje.tests.model.basic.Customer;
import com.avaje.tests.model.basic.ResetBasicData;

public class TestTextJsonInsertUpdate extends TestCase {

    public void test() {

        ResetBasicData.reset();
        
        String json0 = "{\"name\":\"InsJson\",\"status\":\"NEW\"}";
        
        JsonContext jsonContext = Ebean.createJsonContext();
        
        // insert
        Customer c0 = jsonContext.toBean(Customer.class, json0);
        Ebean.save(c0);
        
        // update with optimistic concurrency checking
        String j0 = jsonContext.toJsonString(c0);
        String j1 = StringHelper.replaceString(j0, "InsJson", "Mod1");
        Customer c1 = jsonContext.toBean(Customer.class, j1);
        Ebean.update(c1);

        // update with no optimistic concurrency checking
        String j2 = "{\"id\":"+c0.getId()+",\"name\":\"ModIns\",\"status\":\"ACTIVE\"}";
        Customer c2 = jsonContext.toBean(Customer.class, j2);
        Ebean.update(c2);
        
    }
}
