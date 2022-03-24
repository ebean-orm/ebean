package org.tests.query;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import io.ebean.Query;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestQueryFilterManyOnSecondary extends BaseTestCase {

  @Test
  public void testFilterManyWithSimplePredicate() {

    ResetBasicData.reset();

    Query<Customer> query = DB.find(Customer.class)
      .fetchQuery("orders")
      .where().ilike("name", "Rob%").gt("id", 0)
      .filterMany("orders").eq("status", Order.Status.NEW)
      .query();

    List<Customer> list = query.findList();
    for (Customer customer : list) {
      List<Order> orders = customer.getOrders();
      for (Order order : orders) {
        assertEquals(Order.Status.NEW, order.getStatus());
      }
    }

  }


  @Test
  public void testFilterManyWithPathPredicate() {

    ResetBasicData.reset();

    Query<Order> query = DB.find(Order.class)
      .fetchQuery("details")
      .fetch("details.product", "name")
      .filterMany("details").ilike("product.name", "c%")
      .query();

    List<Order> orders = query.findList();
    for (Order order : orders) {
      List<OrderDetail> details = order.getDetails();
      for (OrderDetail orderDetail : details) {
        Product product = orderDetail.getProduct();
        String name = product.getName();
        assertTrue(name.startsWith("C"));
      }
    }
  }
}
