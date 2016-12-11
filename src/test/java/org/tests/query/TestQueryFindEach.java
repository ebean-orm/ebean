package org.tests.query;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.EbeanServer;
import io.ebean.FetchConfig;
import io.ebean.Query;
import org.tests.model.basic.Customer;
import org.tests.model.basic.ResetBasicData;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

public class TestQueryFindEach extends BaseTestCase {

  @Test
  public void test() {

    ResetBasicData.reset();

    EbeanServer server = Ebean.getServer(null);

    Query<Customer> query
      = server.find(Customer.class)
      .setAutoTune(false)
      .fetch("contacts", new FetchConfig().query(2)).where().gt("id", 0).orderBy("id")
      .setMaxRows(2);

    final AtomicInteger counter = new AtomicInteger(0);

    query.findEach(customer -> {
      counter.incrementAndGet();
      customer.getName();
    });

    Assert.assertEquals(2, counter.get());
  }

  /**
   * Test the behaviour when an exception is thrown inside the findVisit().
   */
  @Test(expected = IllegalStateException.class)
  public void testVisitThrowingException() {

    ResetBasicData.reset();

    EbeanServer server = Ebean.getServer(null);

    Query<Customer> query = server.find(Customer.class).setAutoTune(false)
      .fetch("contacts", new FetchConfig().query(2)).where().gt("id", 0).orderBy("id")
      .setMaxRows(2);

    final AtomicInteger counter = new AtomicInteger(0);

    query.findEach(customer -> {
      counter.incrementAndGet();
      if (counter.intValue() > 0) {
        throw new IllegalStateException("cause a failure");
      }
    });

    Assert.assertFalse("Never get here - exception thrown", true);
  }
}
