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

public class TestDeleteByIdList extends BaseTestCase {

  @Test
  public void test() {

    ResetBasicData.reset();

    OrderDetail dummy = Ebean.getReference(OrderDetail.class, 1);
    SpiEbeanServer server = (SpiEbeanServer) Ebean.getServer(null);
    server.getBeanDescriptor(OrderDetail.class).cacheBeanPut(dummy);

    Customer c0 = ResetBasicData.createCustAndOrder("DelIdList-0");
    Assert.assertNotNull(c0);

    Customer c1 = ResetBasicData.createCustAndOrder("DelIdList-1");

    List<Object> orderIds = Ebean.find(Order.class).where().in("customer", c0, c1).findIds();

    Assert.assertEquals(2, orderIds.size());

    Ebean.deleteAll(Order.class, orderIds);
    Ebean.delete(c0);
    Ebean.delete(c1);

  }
}
