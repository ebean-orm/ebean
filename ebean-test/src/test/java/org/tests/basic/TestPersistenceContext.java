package org.tests.basic;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.Transaction;
import io.ebeaninternal.api.SpiPersistenceContext;
import io.ebeaninternal.api.SpiTransaction;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.ContactNote;
import org.tests.model.basic.Customer;
import org.tests.model.basic.Order;
import org.tests.model.basic.ResetBasicData;

import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class TestPersistenceContext extends BaseTestCase {

  
  @Test
  void testReload() {
    ResetBasicData.reset();
    try (Transaction txn = DB.beginTransaction()) {
      List<ContactNote> notes = new ArrayList<>();
      DB.find(ContactNote.class).findEach(notes::add);
      notes.get(0).setTitle("FooBar");
      DB.find(ContactNote.class).findList();
      assertThat(notes.get(0).getTitle()).isEqualTo("FooBar");
    }
  }
  
  @Test
  void test() {
    ResetBasicData.reset();

    // implicit transaction with its own persistence context
    Order oBefore = DB.find(Order.class, 1);
    // start a persistence context
    DB.beginTransaction();

    Order order;
    try {
      order = DB.find(Order.class, 1);
      // not the same instance ...as a different persistence context
      assertNotSame(order, oBefore);

      // finds an existing bean in the persistence context
      // ... so doesn't even execute a query
      Order o2 = DB.find(Order.class, 1);
      Order o3 = DB.reference(Order.class, 1);

      // all the same instance
      assertSame(order, o2);
      assertSame(order, o3);

    } finally {
      DB.endTransaction();
    }

    // implicit transaction with its own persistence context
    Order oAfter = DB.find(Order.class, 1);
    assertNotSame(oAfter, oBefore);
    assertNotSame(oAfter, order);

    // start a persistence context
    DB.beginTransaction();
    try {
      Order testOrder = ResetBasicData.createOrderCustAndOrder("testPC");
      Integer id = testOrder.getCustomer().getId();
      Integer orderId = testOrder.getId();

      Customer customer = DB.find(Customer.class)
        .setUseCache(false)
        .setId(id)
        .findOne();
      assert customer != null;

      System.gc();
      Order order2 = DB.find(Order.class, orderId);
      assert order2 != null;
      Customer customer2 = order2.getCustomer();
      assert customer2 != null;

      assertEquals(customer.getId(), customer2.getId());
      assertSame(customer, customer2);

    } finally {
      DB.endTransaction();
    }
  }

  @Disabled
  @Test
  void findWithGcTest() {
    for (int j = 0; j < 20; j++) {
      for (int i = 0; i < 500; i++) {
        Customer c = new Customer();
        c.setName("Customer #" + i);
        DB.save(c);
      }
      int customerCount = DB.find(Customer.class).findCount();
      AtomicInteger count = new AtomicInteger(customerCount);

      WeakHashMap<Customer, Integer> customers = new WeakHashMap<>();

      DB.find(Customer.class).fetch("orders").findEach(customer -> {
        customers.put(customer, customer.getId());
        if (count.decrementAndGet() == 0) {
          // Trigger garbage collection on last iteration and check if beans disappear from memory
          System.gc();
          try {
            Thread.sleep(100);
          } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            e.printStackTrace();
          }
          customers.size(); // expunge stale entries
          System.out.println("Total instances: " + customerCount + ", instances left in memory: " + customers.size());
        }
      });
    }
  }

  @Disabled // run manually
  @Test
  void testPcScopes_with_weakReferences() throws InterruptedException {
    for (int i = 0; i < 5000; i++) {
      Customer c = new Customer();
      c.setName("Customer #" + i);
      DB.save(c);
      Order o = new Order();
      o.setCustomer(c);
      DB.save(o);
    }

    try (Transaction txn = DB.beginTransaction()) {
      List<Customer> first100 = DB.find(Customer.class).where().le("id", 100).findList();
      assertEquals(100, first100.size());
      for (Customer c : first100) {
        c.setSmallnote("one of the first 100");
      }

      // use lastBean to hold onto a weak reference bean
      Customer[] lastBean = new Customer[1];
      // findEach switches on use of weak reference in persistence context
      DB.find(Customer.class).setLazyLoadBatchSize(1).findEach(customer -> {
        if (customer.getId() <= 100) {
          assertEquals("one of the first 100", customer.getSmallnote());
          // nested finds
          DB.find(Order.class).where().eq("customer", customer).findEach(20, consumer -> {
          });
        } else {
          assertNotEquals("one of the first 100", customer.getSmallnote());
        }
        lastBean[0] = customer;
      });

      SpiPersistenceContext pc = ((SpiTransaction) txn).getPersistenceContext();
      // the first 100 customers using strong references
      assertThat(pc.toString()).contains("Customer=size:5000 (4900 weak)");
      assertThat(pc.toString()).contains("Order=size:100 (100 weak)");

      System.gc();
      Thread.sleep(100);
      pc.get(Customer.class, 1); // trigger expungeStaleEntries
      // pc.get(Order.class, 1);
      assertThat(pc.toString()).contains("Customer=size:101 (1 weak)");
      assertThat(pc.toString()).contains("Order=size:0 (0 weak)");

      first100 = DB.find(Customer.class).where().le("id", 100).findList();
      for (Customer c : first100) {
        assertEquals("one of the first 100", c.getSmallnote());
      }
      Customer lastBeanFromDb = DB.find(Customer.class).setId(lastBean[0].getId()).findOne();
      assertSame(lastBeanFromDb, lastBean[0]);

      // read 200
      DB.find(Customer.class).where().le("id", 200).findList();
      assertThat(pc.toString()).contains("Customer=size:201 (1 weak)");

      lastBean[0] = null; // allow GC on this one
      lastBeanFromDb = null;
      System.gc();
      Thread.sleep(100);
      assertThat(pc.toString()).contains("Customer=size:200 (0 weak)");
    }
  }
}
