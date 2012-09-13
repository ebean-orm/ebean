package com.avaje.tests.basic.delete;

import java.util.List;

import junit.framework.TestCase;

import com.avaje.ebean.Ebean;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.tests.model.basic.Customer;
import com.avaje.tests.model.basic.Order;
import com.avaje.tests.model.basic.OrderDetail;
import com.avaje.tests.model.basic.ResetBasicData;

public class TestDeleteCascadeById extends TestCase {

    public void test() {
        
        ResetBasicData.reset();
        
        OrderDetail dummy = Ebean.getReference(OrderDetail.class, 1);
        SpiEbeanServer server = (SpiEbeanServer)Ebean.getServer(null);
        server.getBeanDescriptor(OrderDetail.class).cachePutBeanData(dummy);
        
        Customer cust = ResetBasicData.createCustAndOrder("DelCas");
        assertNotNull(cust);
        
        List<Order> orders = Ebean.find(Order.class)
            .where().eq("customer", cust)
            .findList();

        assertEquals(1, orders.size());
        Order o = orders.get(0);
        assertNotNull(o);
        
        Ebean.delete(o);
        
    }
}
