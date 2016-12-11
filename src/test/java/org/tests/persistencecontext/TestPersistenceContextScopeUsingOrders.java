package org.tests.persistencecontext;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import org.tests.model.basic.Customer;
import org.tests.model.basic.Order;
import org.tests.model.basic.ResetBasicData;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static io.ebean.PersistenceContextScope.QUERY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestPersistenceContextScopeUsingOrders extends BaseTestCase {

  @Test
  public void test_persistenceContextScopeQuery() {

    ResetBasicData.reset();

    List<Order> orders = Ebean.find(Order.class)
      // Use QUERY or TRANSACTION scope (just not NONE)
      .setPersistenceContextScope(QUERY)
      .fetch("customer", "id, name")
      .where().istartsWith("customer.name", "rob").eq("customer.id", 1)
      .orderBy().asc("customer.name")
      .findList();

    assertTrue(!orders.isEmpty());

    // collect the customer instances
    List<Customer> customers = new ArrayList<>();
    Set<Integer> identities = new HashSet<>();

    for (Order order : orders) {
      Customer customer = order.getCustomer();
      identities.add(System.identityHashCode(customer));
      customers.add(customer);
    }

    // only one instance of Customer this time ...
    assertEquals(1, identities.size());
    assertTrue(customers.size() > 1);
  }

}
