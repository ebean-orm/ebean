package org.tests.batchload;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestLazyJoin2 extends BaseTestCase {

  @Test
  public void testLazyOnNonLoaded() {

    ResetBasicData.reset();

    // This will use 3 SQL queries to build this object graph
    List<Order> l0 = DB.find(Order.class).select("status, shipDate")

      .fetchQuery("details", "orderQty, unitPrice")
      .fetch("details.product", "sku, name")

      .fetchQuery("customer", "name")
      .fetch("customer.contacts", "firstName, lastName, mobile")
      .fetch("customer.shippingAddress", "line1, city").order().asc("id").findList();

    Order o0 = l0.get(0);
    Customer c0 = o0.getCustomer();
    List<Contact> contacts = c0.getContacts();
    assertTrue(!contacts.isEmpty());

    // query 1) find order (status, shipDate)
    // query 2) find orderDetail (quantity, price) join product (sku, name)
    // where order.id in (?,? ...)
    // query 3) find customer (name) join contacts (*) join shippingAddress (*)
    // where id in (?,?,?,?,?)

    List<Order> orders = DB.find(Order.class)
      // .select("status")
      .fetchQuery("customer").order().asc("id").findList();
    // .join("customer.contacts");

    // List<Order> list = query.findList();

    Order order = orders.get(0);
    Customer customer = order.getCustomer();

    // this invokes lazy loading on a property that is
    // not one of the selected ones (name, status) ... and
    // therefore the lazy load query selects all properties
    // in the customer (not just name and status)
    Address billingAddress = customer.getBillingAddress();

    assertNotNull(billingAddress);

    List<Order> list = DB.find(Order.class).fetchLazy("customer", "name")
      .fetch("customer.contacts", "firstName, lastName, phone, email").fetch("customer.shippingAddress")
      .where().eq("status", Order.Status.NEW).order().asc("id").findList();

    Order order2 = list.get(0);
    Customer customer2 = order2.getCustomer();
    // customer2.getStatus();
    String name = customer2.getName();
    assertNotNull(name);

  }

}
