package org.tests.basic.delete;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebeaninternal.api.SpiEbeanServer;
import org.tests.model.basic.Customer;
import org.tests.model.basic.Order;
import org.tests.model.basic.OrderDetail;
import org.tests.model.basic.ResetBasicData;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TestDeleteByIdList extends BaseTestCase {

  @Test
  public void test() {

    ResetBasicData.reset();

    OrderDetail dummy = DB.reference(OrderDetail.class, 1);
    SpiEbeanServer server = (SpiEbeanServer) DB.getDefault();
    server.getBeanDescriptor(OrderDetail.class).cacheBeanPut(dummy);

    Customer c0 = ResetBasicData.createCustAndOrder("DelIdList-0");
    assertNotNull(c0);

    Customer c1 = ResetBasicData.createCustAndOrder("DelIdList-1");

    List<Object> orderIds = DB.find(Order.class).where().in("customer", c0, c1).findIds();

    assertEquals(2, orderIds.size());

    DB.deleteAll(Order.class, orderIds);
    DB.delete(c0);
    DB.delete(c1);

  }
}
