package org.tests.batchload;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.Expr;
import io.ebean.FetchConfig;
import io.ebean.Transaction;
import org.tests.basic.MyTestDataSourcePoolListener;
import org.tests.model.basic.Address;
import org.tests.model.basic.Contact;
import org.tests.model.basic.Customer;
import org.tests.model.basic.Order;
import org.tests.model.basic.ResetBasicData;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class TestBasicLazy extends BaseTestCase {

  @Test
  public void testQueries() {

    ResetBasicData.reset();

    Order order = Ebean.find(Order.class).select("totalAmount").setMaxRows(1).order("id")
      .findOne();

    Assert.assertNotNull(order);

    Customer customer = order.getCustomer();
    Assert.assertNotNull(customer);
    Assert.assertNotNull(customer.getName());

    Address address = customer.getBillingAddress();
    Assert.assertNotNull(address);
    Assert.assertNotNull(address.getCity());
  }

  public void test_N1N() {
    ResetBasicData.reset();

    // safety check to see if our customer we are going to use for the test has
    // some contacts
    Customer c = Ebean.find(Customer.class).setId(1).findOne();
    Assert.assertNotNull(c.getContacts());
    Assert.assertTrue("no contacts on test customer 1", !c.getContacts().isEmpty());

    // start transaction so we have a "long running" persistence context
    Transaction tx = Ebean.beginTransaction();
    try {
      List<Order> order = Ebean.find(Order.class).where(Expr.eq("customer.id", 1)).findList();

      Assert.assertNotNull(order);
      Assert.assertTrue(!order.isEmpty());

      Customer customer = order.get(0).getCustomer();
      Assert.assertNotNull(customer);
      Assert.assertEquals(1, customer.getId().intValue());

      // this should lazily fetch the contacts
      List<Contact> contacts = customer.getContacts();

      Assert.assertNotNull(contacts);
      Assert.assertTrue("contacts not lazily fetched", !contacts.isEmpty());
    } finally {
      tx.commit();
    }
  }

  public void testRaceCondition_Simple() throws Throwable {
    ResetBasicData.reset();

    Order order = Ebean.find(Order.class).select("totalAmount").setMaxRows(1).order("id")
      .findOne();

    Assert.assertNotNull(order);

    final Customer customer = order.getCustomer();
    Assert.assertNotNull(customer);

    Assert.assertTrue(Ebean.getBeanState(customer).isReference());

    final Throwable throwables[] = new Throwable[2];
    Thread t1 = new Thread() {
      @Override
      public void run() {
        try {
          Assert.assertNotNull(customer.getName());
        } catch (Throwable e) {
          throwables[0] = e;
        }
      }
    };

    Thread t2 = new Thread() {
      @Override
      public void run() {
        try {
          Assert.assertNotNull(customer.getName());
        } catch (Throwable e) {
          throwables[1] = e;
        }
      }
    };

    try {
      // prepare for race condition
      MyTestDataSourcePoolListener.SLEEP_AFTER_BORROW = 2000;

      t1.start();
      t2.start();
      t1.join();
      t2.join();
    } finally {
      MyTestDataSourcePoolListener.SLEEP_AFTER_BORROW = 0;
    }

    Assert.assertFalse(Ebean.getBeanState(customer).isReference());

    if (throwables[0] != null) {
      throw throwables[0];
    }
    if (throwables[1] != null) {
      throw throwables[1];
    }
  }

  private final AtomicBoolean mutex = new AtomicBoolean(false);
  private List<Order> orders;

  private List<Throwable> exceptions = Collections.synchronizedList(new ArrayList<Throwable>());

  private class FetchThread extends Thread {
    private int index;

    private FetchThread(ThreadGroup tg, int index) {
      super(tg, "fetcher-" + index);
      this.index = index;
    }

    @Override
    public void run() {
      synchronized (mutex) {
        System.err.println("** WAIT **");
        try {
          while (!mutex.get()) {
            mutex.wait(100);
          }
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
      }

      try {
        System.err.println("** DO LAZY FETCH **");
        orders.get(index).getCustomer().getName();
      } catch (Throwable e) {
        exceptions.add(e);
      }
    }
  }

  public void testRaceCondition_Complex() throws Throwable {
    ResetBasicData.reset();

    ThreadGroup tg = new ThreadGroup("fetchers");
    new FetchThread(tg, 0).start();
    new FetchThread(tg, 1).start();
    new FetchThread(tg, 2).start();
    new FetchThread(tg, 3).start();
    new FetchThread(tg, 0).start();
    new FetchThread(tg, 1).start();
    new FetchThread(tg, 2).start();
    new FetchThread(tg, 3).start();

    orders = Ebean.find(Order.class).fetch("customer", new FetchConfig().lazy(100)).findList();
    Assert.assertTrue(orders.size() >= 4);

    try {
      MyTestDataSourcePoolListener.SLEEP_AFTER_BORROW = 2000;

      synchronized (mutex) {
        mutex.set(true);
        mutex.notifyAll();
      }

      while (tg.activeCount() > 0) {
        Thread.sleep(100);
      }
    } finally {
      MyTestDataSourcePoolListener.SLEEP_AFTER_BORROW = 0;
    }

    if (!exceptions.isEmpty()) {
      System.err.println("Seen Exceptions:");
      for (Throwable exception : exceptions) {
        exception.printStackTrace();
      }
      Assert.fail();
    }
  }
}
