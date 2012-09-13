package com.avaje.tests.transaction;

import java.util.List;

import junit.framework.TestCase;

import org.junit.Assert;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.Transaction;
import com.avaje.ebean.bean.PersistenceContext;
import com.avaje.ebeaninternal.api.SpiTransaction;
import com.avaje.tests.model.basic.Customer;
import com.avaje.tests.model.basic.Order;
import com.avaje.tests.model.basic.ResetBasicData;

public class TestPersistContextClear extends TestCase {

    public void test() {
        
        ResetBasicData.reset();
        
        
        ResetBasicData.createOrderCustAndOrder("testPc");
        
        Transaction t  = Ebean.beginTransaction();
        SpiTransaction spiTxn = (SpiTransaction)t;
        PersistenceContext pc = spiTxn.getPersistenceContext();
        
        System.out.println("pc0:"+pc.toString());

        // no orders or customers in the PC
        Assert.assertEquals(0, pc.size(Order.class));
        Assert.assertEquals(0, pc.size(Customer.class));

        Order order0 = null;
        try {
            
            EbeanServer server = Ebean.getServer(null);
            List<Order> list = server.find(Order.class)
                .fetch("customer")
                .fetch("details")
                .findList();

            int orderSize = list.size();
            Assert.assertTrue(orderSize > 1);
            
            // keep a hold of one of them
            order0 = list.get(0);
            
            System.out.println("pc1:"+pc.toString());
            Assert.assertEquals(orderSize, pc.size(Order.class));

            System.gc();
            Assert.assertEquals(orderSize, pc.size(Order.class));
            Assert.assertTrue(pc.size(Customer.class) > 0);

            list = null;
            //System.gc();

            // transaction still holds PC ...
            //System.out.println("pc2:"+pc);
            // These asserts may not succeed depending on JVM
            //Assert.assertEquals(pc.size(Order.class), 1);
            //Assert.assertEquals(pc.size(Customer.class), 1);
            
        } finally {
            t.end();
        }
        
        System.gc();
        System.out.println("pc4:"+pc.toString());
        
        // we still have the order
        Assert.assertNotNull(order0);
        // its likely the only one in the PC now
        // due to the System.gc(); but can't garuntee it 
        // so removing these asserts... can put them back
        // to manually test.
//        Assert.assertEquals(pc.size(Order.class), 1);
//        Assert.assertEquals(pc.size(Customer.class), 1);
        

    }
    
}
