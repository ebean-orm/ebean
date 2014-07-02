package com.avaje.tests.batchload;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.FetchConfig;
import com.avaje.ebean.Query;
import com.avaje.tests.model.basic.Address;
import com.avaje.tests.model.basic.Contact;
import com.avaje.tests.model.basic.Customer;
import com.avaje.tests.model.basic.Order;
import com.avaje.tests.model.basic.ResetBasicData;

public class TestLazyJoin2 extends BaseTestCase {

  @Test
  public void testLazyOnNonLoaded() {

    ResetBasicData.reset();

    // This will use 3 SQL queries to build this object graph
    List<Order> l0 = Ebean.find(Order.class).select("status, shipDate")

    .fetch("details", "orderQty, unitPrice", new FetchConfig().query())
        .fetch("details.product", "sku, name")

        .fetch("customer", "name", new FetchConfig().query(10))
        .fetch("customer.contacts", "firstName, lastName, mobile")
        .fetch("customer.shippingAddress", "line1, city").order().asc("id").findList();

    Order o0 = l0.get(0);
    Customer c0 = o0.getCustomer();
    List<Contact> contacts = c0.getContacts();
    Assert.assertTrue(contacts.size() > 0);

    // query 1) find order (status, shipDate)
    // query 2) find orderDetail (quantity, price) join product (sku, name)
    // where order.id in (?,? ...)
    // query 3) find customer (name) join contacts (*) join shippingAddress (*)
    // where id in (?,?,?,?,?)

    List<Order> orders = Ebean.find(Order.class)
    // .select("status")
        .fetch("customer", new FetchConfig().query(3).lazy(10)).order().asc("id").findList();
    // .join("customer.contacts");

    // List<Order> list = query.findList();

    Order order = orders.get(0);
    Customer customer = order.getCustomer();

    // this invokes lazy loading on a property that is
    // not one of the selected ones (name, status) ... and
    // therefore the lazy load query selects all properties
    // in the customer (not just name and status)
    Address billingAddress = customer.getBillingAddress();

    Assert.assertNotNull(billingAddress);

    List<Order> list = Ebean.find(Order.class).fetch("customer", "name", new FetchConfig().lazy(5))
        .fetch("customer.contacts", "contactName, phone, email").fetch("customer.shippingAddress")
        .where().eq("status", Order.Status.NEW).order().asc("id").findList();

    Order order2 = list.get(0);
    Customer customer2 = order2.getCustomer();
    // customer2.getStatus();
    String name = customer2.getName();
    Assert.assertNotNull(name);

    String q = "find order join customer (+query(1) +lazy(10) name, status) join customer.contacts ";
    Query<Order> query = Ebean.createQuery(Order.class, q);

    List<Order> list3 = query.findList();
    Order order3 = list3.get(0);
    String n3 = order3.getCustomer().getName();

    for (Order o3 : list3) {
      o3.getCustomer().getName();
    }

    Assert.assertNotNull(n3);

  }

}
