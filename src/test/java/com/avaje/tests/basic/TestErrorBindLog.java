package com.avaje.tests.basic;

import javax.persistence.PersistenceException;

import junit.framework.TestCase;

import org.junit.Assert;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.config.GlobalProperties;
import com.avaje.tests.model.basic.Order;

public class TestErrorBindLog extends TestCase {

    public void test() {
        
        GlobalProperties.put("somethingelse", "d:/junk2");
        try {
            Ebean.find(Order.class)
                .where().gt("id", "JUNK")
                .findList();
        
        } catch (PersistenceException e){
            String msg = e.getMessage();
            e.printStackTrace();
            Assert.assertTrue(msg.contains("Bind values:"));
        }
    }
}
