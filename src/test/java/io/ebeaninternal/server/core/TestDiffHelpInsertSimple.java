package io.ebeaninternal.server.core;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.EbeanServer;
import io.ebean.ValuePair;
import io.ebean.bean.EntityBean;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import org.tests.model.basic.Customer;
import org.tests.model.basic.Order;
import org.tests.model.basic.Order.Status;
import org.junit.Test;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

public class TestDiffHelpInsertSimple extends BaseTestCase {

  long firstTime = System.currentTimeMillis() - 10000;

  EbeanServer server;

  BeanDescriptor<Order> orderDesc;

  public TestDiffHelpInsertSimple() {
    server = Ebean.getServer(null);
    SpiEbeanServer spiServer = (SpiEbeanServer) server;
    orderDesc = spiServer.getBeanDescriptor(Order.class);
  }

  private Order createBaseOrder(EbeanServer server) {
    Order order1 = new Order();
    order1.setId(12);
    order1.setCretime(new Timestamp(firstTime));
    order1.setCustomer(server.getReference(Customer.class, 1234));
    order1.setStatus(Status.NEW);
    //order1.setShipDate(new Date(firstTime));
    order1.setOrderDate(new Date(firstTime));
    return order1;
  }

  @Test
  public void basic() {

    Order order1 = createBaseOrder(server);

    Map<String, ValuePair> diff = orderDesc.diffForInsert((EntityBean) order1);

    assertEquals(4, diff.size());

    Set<String> keySet = diff.keySet();
    assertTrue(keySet.contains("cretime"));
    assertTrue(keySet.contains("status"));
    assertTrue(keySet.contains("orderDate"));
    assertTrue(keySet.contains("customer.id"));
    assertFalse(keySet.contains("shipDate"));
  }

}
