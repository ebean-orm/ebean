package com.avaje.tests.subclass;

import junit.framework.TestCase;

import org.junit.Assert;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebean.config.GlobalProperties;
import com.avaje.tests.model.basic.EBasicNoDefaultConstructor;

public class TestSubclassNewInstance extends TestCase {

    public void test() {
        
        GlobalProperties.put("ebean.classes", EBasicNoDefaultConstructor.class.toString());
        
        EBasicNoDefaultConstructor inst = new EBasicNoDefaultConstructor(12,"banana");
        
        if (inst instanceof EntityBean){
            // using enhancement so can't run test
            return;
        }
        
        EBasicNoDefaultConstructor inst2 = Ebean.getServer(null).createEntityBean(EBasicNoDefaultConstructor.class);
        
        Assert.assertTrue(inst2 instanceof EntityBean);
        
        EntityBean eb = (EntityBean)inst2;
        Object inst3 = eb._ebean_newInstance();
     
        Assert.assertTrue(inst3 instanceof EntityBean);
        Assert.assertTrue(inst3 instanceof EBasicNoDefaultConstructor);
        
        EBasicNoDefaultConstructor e3 = (EBasicNoDefaultConstructor)inst3;
        e3.getName();
        
        
    }
}
