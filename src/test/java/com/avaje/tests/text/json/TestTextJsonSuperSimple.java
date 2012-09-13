package com.avaje.tests.text.json;

import java.util.List;

import junit.framework.TestCase;

import org.junit.Assert;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.text.json.JsonContext;
import com.avaje.tests.model.basic.Customer;
import com.avaje.tests.model.basic.ResetBasicData;

public class TestTextJsonSuperSimple extends TestCase {

    public void test() {
        
        ResetBasicData.reset();
        
        List<Customer> list = Ebean.find(Customer.class)
            .select("id, name")
            .order().desc("id")
            .findList();
        
        EbeanServer server = Ebean.getServer(null);

        JsonContext json = server.createJsonContext();
        
        if (list.size() > 1){
        	Customer customer = list.get(0);
        	
            String s = json.toJsonString(customer, true);
            System.out.println(s);
            int statusPos = s.indexOf("status");
            Assert.assertEquals(-1, statusPos);
        }

    }
}
