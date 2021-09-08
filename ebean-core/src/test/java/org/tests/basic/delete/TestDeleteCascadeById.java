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

public class TestDeleteCascadeById extends BaseTestCase {

  @Test
  public void test() {

    ResetBasicData.reset();

    OrderDetail dummy = DB.reference(OrderDetail.class, 1);
    SpiEbeanServer server = (SpiEbeanServer) DB.getDefault();
    server.descriptor(OrderDetail.class).cacheBeanPut(dummy);

    Customer cust = ResetBasicData.createCustAndOrder("DelCas");
    assertNotNull(cust);

    List<Order> orders = DB.find(Order.class).where().eq("customer", cust).findList();

    assertEquals(1, orders.size());
    Order o = orders.get(0);
    assertNotNull(o);

    // cleanup
    DB.delete(o);
    DB.delete(cust);

  }
}
