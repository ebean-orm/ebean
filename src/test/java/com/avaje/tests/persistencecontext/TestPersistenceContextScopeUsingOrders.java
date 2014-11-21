package com.avaje.tests.persistencecontext;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.PersistenceContextScope;
import com.avaje.tests.model.basic.Customer;
import com.avaje.tests.model.basic.Order;
import com.avaje.tests.model.basic.ResetBasicData;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.avaje.ebean.PersistenceContextScope.QUERY;
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
    List<Customer> customers = new ArrayList<Customer>();
    Set<Integer> identities = new HashSet<Integer>();

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
