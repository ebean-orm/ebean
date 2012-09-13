package com.avaje.tests.m2m;

import javax.persistence.PersistenceException;

import junit.framework.TestCase;

import org.junit.Assert;

import com.avaje.ebean.Ebean;
import com.avaje.tests.model.basic.MnocRole;
import com.avaje.tests.model.basic.MnocUser;

public class TestM2MDeleteNoCascade extends TestCase {

    public void test() {
        
        MnocRole r0 = new MnocRole("r0");
        MnocRole r1 = new MnocRole("r0");
        
        Ebean.save(r0);
        Ebean.save(r1);
        
        MnocUser u0 = new MnocUser("usr0");
        u0.addValidRole(r0);
        u0.addValidRole(r1);
        
        Ebean.save(u0);
        
        try {
            Ebean.delete(u0);
            Assert.assertTrue("expecting an exception",false);
            
        } catch (PersistenceException e){
            // we expect this
            Assert.assertTrue(true);
        }
        
        int rc = Ebean.deleteManyToManyAssociations(u0, "validRoles");
        Assert.assertTrue(rc != 0);
        
        Ebean.delete(u0);
        Assert.assertTrue(true);
        
    }
}
