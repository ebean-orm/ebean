package com.avaje.tests.query;

import javax.persistence.PersistenceException;

import com.avaje.ebean.*;
import com.avaje.tests.model.basic.Order;
import com.avaje.tests.model.basic.OrderShipment;
import org.avaje.ebeantest.LoggedSqlCollector;
import org.junit.Test;

import com.avaje.tests.model.basic.Customer;
import com.avaje.tests.model.basic.ResetBasicData;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestQueryFindIterate extends BaseTestCase {

  @Test
  public void test() {

    ResetBasicData.reset();

    EbeanServer server = Ebean.getServer(null);

    Query<Customer> query = server.find(Customer.class)
        .setAutofetch(false)
        //.fetch("contacts", new FetchConfig().query(2)).where().gt("id", 0).orderBy("id")
        .setMaxRows(2);

    int count = 0;

    QueryIterator<Customer> it = query.findIterate();
    try {
      while (it.hasNext()) {
        Customer customer = it.next();
        customer.hashCode();
        count++;
      }
    } finally {
      it.close();
    }

    assertEquals(2, count);
  }

  @Test
  public void testWithLazyLoading() {

    ResetBasicData.reset();

    QueryIterator<Order> queryIterator = Ebean.find(Order.class)
            //.select("orderDate")
            .where().gt("id",0).le("id",10)
            .findIterate();

    try {
      while (queryIterator.hasNext()) {
        Order order = queryIterator.next();
        Customer customer = order.getCustomer();
        // invoke lazy loading on customer, order details and order shipments
        System.out.println("order: " + order.getId() + " customerName:" + customer.getName()+" details:"+order.getDetails().size()+" shipments:"+order.getShipments().size());
      }

    } finally {
      queryIterator.close();
    }

  }

  @Test
  public void testWithLazyBatchSize() {

    ResetBasicData.reset();

    LoggedSqlCollector.start();

    QueryIterator<Order> queryIterator = Ebean.find(Order.class)
            .setLazyLoadBatchSize(10)
            .select("status, orderDate")
            .fetch("customer", "name")
            .where().gt("id",0).le("id",10)
            .findIterate();

    try {
      while (queryIterator.hasNext()) {
        Order order = queryIterator.next();
        Customer customer = order.getCustomer();
        System.out.println("order: " + order.getId() + " customerName:" + customer.getName()+" details:"+order.getDetails().size()+" shipments:"+order.getShipments().size());
      }

    } finally {
      queryIterator.close();
    }

    List<String> loggedSql = LoggedSqlCollector.stop();

    assertEquals(3, loggedSql.size());
    assertTrue(loggedSql.get(0).contains("select t0.id c0, t0.status c1, t0.order_date c2, t1.id c3, t1.name c4 from o_order t0 join o_customer t1"));
    assertTrue(loggedSql.get(1).contains("select t0.order_id c0, t0.id c1, t0.order_qty c2, t0.ship_qty c3, t0.unit_price c4"));
    assertTrue(loggedSql.get(2).contains("select t0.order_id c0, t0.id c1, t0.ship_time c2, t0.cretime c3, t0.updtime c4, t0.order_id c5 from or_order_ship"));
  }

  @Test
  public void testWithTwoJoins() {

    ResetBasicData.reset();

    LoggedSqlCollector.start();

    // make sure we don't hit the L2 cache for order shipments
    Ebean.getServerCacheManager().getBeanCache(Order.class).clear();
    Ebean.getServerCacheManager().getQueryCache(Order.class).clear();
    Ebean.getServerCacheManager().getBeanCache(OrderShipment.class).clear();
    Ebean.getServerCacheManager().getQueryCache(OrderShipment.class).clear();

    QueryIterator<Order> queryIterator = Ebean.find(Order.class)
            .setLazyLoadBatchSize(10)
            .setUseCache(false)
            .setUseQueryCache(false)
            .select("status, orderDate")
            .fetch("customer", "name")
            .fetch("details")
            .where().gt("id",0).le("id",10)
            .order().asc("id")
            .findIterate();

    try {
      while (queryIterator.hasNext()) {
        Order order = queryIterator.next();
        Customer customer = order.getCustomer();
        System.out.println("order: " + order.getId() + " customerName:" + customer.getName()+" details:"+order.getDetails().size()+" shipments:"+order.getShipments().size());
      }

    } finally {
      queryIterator.close();
    }

    List<String> loggedSql = LoggedSqlCollector.stop();

    assertEquals("Got SQL: "+loggedSql, 2, loggedSql.size());
    assertTrue(loggedSql.get(0).contains("select t0.id c0, t0.status c1, t0.order_date c2, t1.id c3, t1.name c4, t2.id c5, t2.order_qty c6, t2.ship_qty"));
    assertTrue(loggedSql.get(1).contains("select t0.order_id c0, t0.id c1, t0.ship_time c2, t0.cretime c3, t0.updtime c4, t0.order_id c5 from or_order_ship"));
  }

  @Test(expected=PersistenceException.class)
  public void testWithExceptionInQuery() {

    ResetBasicData.reset();

    EbeanServer server = Ebean.getServer(null);

    // intentionally a query with incorrect type binding
    Query<Customer> query = server.find(Customer.class)
        .setAutofetch(false)
        .where().gt("id","JUNK_NOT_A_LONG")
        .setMaxRows(2);

    // this throws an exception immediately
    QueryIterator<Customer> it = query.findIterate();
    it.hashCode();
    assertTrue("Never get here as exception thrown", false);
  }
  
  
  @Test(expected=IllegalStateException.class)
  public void testWithExceptionInLoop() {

    ResetBasicData.reset();

    EbeanServer server = Ebean.getServer(null);

    Query<Customer> query = server.find(Customer.class)
        .setAutofetch(false)
        .where().gt("id", 0)
        .setMaxRows(2);

    QueryIterator<Customer> it = query.findIterate();
    try {
      while (it.hasNext()) {
        Customer customer = it.next();
        if (customer != null) {
          throw new IllegalStateException("cause an exception");
        }
      }
      
    } finally {
      it.close();
    }
  }
}
