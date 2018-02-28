package org.tests.basic.delete;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebeaninternal.api.SpiEbeanServer;
import org.tests.model.basic.Customer;
import org.tests.model.basic.Order;
import org.tests.model.basic.OrderDetail;
import org.tests.model.basic.ResetBasicData;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class TestDeleteCascadeById extends BaseTestCase {

  @Test
  public void test() {

    ResetBasicData.reset();

    OrderDetail dummy = Ebean.getReference(OrderDetail.class, 1);
    SpiEbeanServer server = (SpiEbeanServer) Ebean.getServer(null);
    server.getBeanDescriptor(OrderDetail.class).cacheBeanPut(dummy);

    Customer cust = ResetBasicData.createCustAndOrder("DelCas");
    Assert.assertNotNull(cust);

    List<Order> orders = Ebean.find(Order.class).where().eq("customer", cust).findList();

    Assert.assertEquals(1, orders.size());
    Order o = orders.get(0);
    Assert.assertNotNull(o);

    // cleanup
    Ebean.delete(o);
    Ebean.delete(cust);

  }
}
