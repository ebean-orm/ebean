package io.ebeaninternal.server.core;

import io.ebean.DB;
import io.ebean.Database;
import io.ebean.ValuePair;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Customer;
import org.tests.model.basic.Order;
import org.tests.model.basic.Order.Status;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class TestDiffHelpSimple {

  long firstTime = System.currentTimeMillis() - 10000;
  long secondTime = System.currentTimeMillis();

  Database server;
  BeanDescriptor<Order> orderDesc;

  TestDiffHelpSimple() {
    server = DB.getDefault();
    SpiEbeanServer spiServer = (SpiEbeanServer) server;
    orderDesc = spiServer.descriptor(Order.class);
  }

  private Order createBaseOrder(Database server) {
    Order order1 = new Order();
    order1.setId(12);
    order1.setCretime(new Timestamp(firstTime));
    order1.setCustomer(server.reference(Customer.class, 1234));
    order1.setStatus(Status.NEW);
    order1.setShipDate(new Date(firstTime));
    order1.setOrderDate(new Date(firstTime));
    return order1;
  }

  @Test
  void diffWhenNull_expect_empty() {
    assertThat(DB.diff(null, null)).isEmpty();
  }

  @Test
  void testBasicChanges() {
    Order order1 = createBaseOrder(server);

    Order order2 = new Order();
    order2.setId(14);
    order2.setCretime(new Timestamp(secondTime));
    order2.setCustomer(server.reference(Customer.class, 2133));
    order2.setStatus(Status.COMPLETE);
    order2.setShipDate(new Date(secondTime));
    order2.setOrderDate(new Date(secondTime));

    Map<String, ValuePair> diff = DiffHelp.diff(order1, order2, orderDesc);

    assertEquals(5, diff.size());

    Set<String> keySet = diff.keySet();
    assertTrue(keySet.contains("cretime"));
    assertTrue(keySet.contains("status"));
    assertTrue(keySet.contains("shipDate"));
    assertTrue(keySet.contains("orderDate"));
    assertTrue(keySet.contains("customer.id"));
  }

  @Test
  void testBasicChanges_given_flatMode() {
    Order order1 = createBaseOrder(server);

    Order order2 = new Order();
    order2.setId(14);
    order2.setCretime(new Timestamp(secondTime));
    order2.setCustomer(server.reference(Customer.class, 2133));
    order2.setStatus(Status.COMPLETE);
    order2.setShipDate(new Date(secondTime));
    order2.setOrderDate(new Date(secondTime));

    Map<String, ValuePair> diff = DiffHelp.diff(order1, order2, orderDesc);

    assertEquals(5, diff.size());

    Set<String> keySet = diff.keySet();
    assertTrue(keySet.contains("cretime"));
    assertTrue(keySet.contains("status"));
    assertTrue(keySet.contains("shipDate"));
    assertTrue(keySet.contains("orderDate"));
    assertTrue(keySet.contains("customer.id"));
    assertEquals(2133, diff.get("customer.id").getOldValue());
    assertEquals(1234, diff.get("customer.id").getNewValue());
  }

  @Test
  void testIdIgnored() {
    Order order1 = createBaseOrder(server);
    Order order2 = createBaseOrder(server);
    order2.setId(14);

    Map<String, ValuePair> diff = DiffHelp.diff(order1, order2, orderDesc);

    assertEquals(0, diff.size());
  }

  @Test
  void testSecondValueNull() {
    Order order1 = createBaseOrder(server);

    Order order2 = createBaseOrder(server);
    order2.setCustomer(server.reference(Customer.class, 2133));
    order2.setStatus(Status.COMPLETE);
    order2.setShipDate(null);

    Map<String, ValuePair> diff = DiffHelp.diff(order1, order2, orderDesc);

    assertEquals(3, diff.size());

    Set<String> keySet = diff.keySet();
    assertTrue(keySet.contains("status"));
    assertTrue(keySet.contains("customer.id"));
    assertTrue(keySet.contains("shipDate"));

    ValuePair shipDatePair = diff.get("shipDate");
    assertEquals(order1.getShipDate(), shipDatePair.getNewValue());
    assertEquals(order2.getShipDate(), shipDatePair.getOldValue());
    assertNull(shipDatePair.getOldValue());
  }

  @Test
  void testFirstValueNull() {
    Order order1 = createBaseOrder(server);
    order1.setShipDate(null);

    Order order2 = createBaseOrder(server);
    order2.setShipDate(new Date(secondTime));

    Map<String, ValuePair> diff = DiffHelp.diff(order1, order2, orderDesc);

    assertEquals(1, diff.size());
    Set<String> keySet = diff.keySet();
    assertTrue(keySet.contains("shipDate"));

    ValuePair shipDatePair = diff.get("shipDate");
    assertEquals(order1.getShipDate(), shipDatePair.getNewValue());
    assertEquals(order2.getShipDate(), shipDatePair.getOldValue());
    assertNull(shipDatePair.getNewValue());
  }

  @Test
  void testBothNull() {
    Order order1 = createBaseOrder(server);
    order1.setShipDate(null);

    Order order2 = createBaseOrder(server);
    order2.setShipDate(null);

    Map<String, ValuePair> diff = DiffHelp.diff(order1, order2, orderDesc);

    assertEquals(0, diff.size());
  }
}
