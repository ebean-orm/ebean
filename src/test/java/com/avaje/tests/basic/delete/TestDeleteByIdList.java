package com.avaje.tests.basic.delete;

import java.util.List;

import junit.framework.TestCase;

import com.avaje.ebean.Ebean;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.tests.model.basic.Customer;
import com.avaje.tests.model.basic.Order;
import com.avaje.tests.model.basic.OrderDetail;
import com.avaje.tests.model.basic.ResetBasicData;

public class TestDeleteByIdList extends TestCase {

    public void test() {
        
        ResetBasicData.reset();
        
        OrderDetail dummy = Ebean.getReference(OrderDetail.class, 1);
        SpiEbeanServer server = (SpiEbeanServer)Ebean.getServer(null);
        server.getBeanDescriptor(OrderDetail.class).cachePutBeanData(dummy);
        
        Customer c0 = ResetBasicData.createCustAndOrder("DelIdList-0");
        assertNotNull(c0);

        Customer c1 = ResetBasicData.createCustAndOrder("DelIdList-1");

        List<Object> orderIds = Ebean.find(Order.class)
            .where().in("customer", c0,c1)
            .findIds();

        assertEquals(2, orderIds.size());
        
        
        Ebean.delete(Order.class, orderIds);
        
    }
}
