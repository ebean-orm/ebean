package com.avaje.tests.basic.vanilla;

import java.util.List;

import junit.framework.Assert;
import junit.framework.TestCase;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebean.config.GlobalProperties;
import com.avaje.tests.model.basic.Order;
import com.avaje.tests.model.basic.ResetBasicData;

public class TestVanillaQuery extends TestCase {

    
    public void test() {
        
        Order beanEnhancedCheck = new Order();
        
        if (beanEnhancedCheck instanceof EntityBean){
            // test only real when not using enhancement
            System.out.println("Not testing TestVanillaQuery as beans are enhanced");
            return;
        }

        // These settings only work when test run standalone (Ebean not booted yet)
        GlobalProperties.put("ebean.vanillaMode", "true");
        GlobalProperties.put("ebean.vanillaRefMode", "true");

        // actually only a vanilla class when using subclass generation 
        Class<?> vanillaClass = Order.class;

//        // ONLY Testing this when running test manually/standalone at this stage        
//        Order oref = Ebean.getReference(Order.class, 1);
//        Class<?> refClass = oref.getClass();
//        Assert.assertEquals(vanillaClass, refClass);
        
        ResetBasicData.reset();
        
        List<Order> list = 
            Ebean.find(Order.class)
            .fetch("details")
            .setVanillaMode(true)
            .findList();
        
        Assert.assertTrue(list.size() > 0);
        
        Order o = list.get(0);
        
        // actually only a vanilla class when using subclass generation 
        Class<?> returnedClass = o.getClass();
        Assert.assertEquals(vanillaClass, returnedClass);
        
        Ebean.refreshMany(o, "details");
        
        Ebean.refresh(o);
        
        if (!(o instanceof EntityBean)){
            // using subclass generation ...
            list = 
                Ebean.find(Order.class)
                .setVanillaMode(false)
                .findList();
    
            Class<?> entityBeanClass = list.get(0).getClass();
            
            Assert.assertNotSame(vanillaClass, entityBeanClass);
        }
    }
}
