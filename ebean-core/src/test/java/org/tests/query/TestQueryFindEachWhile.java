package org.tests.query;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.FetchConfig;
import io.ebean.Query;
import org.junit.Test;
import org.tests.model.basic.Customer;
import org.tests.model.basic.ResetBasicData;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

public class TestQueryFindEachWhile extends BaseTestCase {

  @Test
  public void test() {

    ResetBasicData.reset();

    Query<Customer> query
      = DB.find(Customer.class)
      .setAutoTune(false)
      .fetchQuery("contacts").where().gt("id", 0).order("id")
      .setMaxRows(2).query();

    final AtomicInteger counter = new AtomicInteger(0);

    query.findEachWhile(customer -> {
      counter.incrementAndGet();
      assertNotNull(customer.getName());
      return true;
    });

    assertEquals(2, counter.get());
  }

  /**
   * Test the behaviour when an exception is thrown inside the findVisit().
   */
  @Test(expected = IllegalStateException.class)
  public void testVisitThrowingException() {

    ResetBasicData.reset();

    Query<Customer> query = DB.find(Customer.class).setAutoTune(false)
      .fetchQuery("contacts").where().gt("id", 0).order("id")
      .setMaxRows(2).query();

    final AtomicInteger counter = new AtomicInteger(0);

    query.findEachWhile(customer -> {
      counter.incrementAndGet();
      if (counter.intValue() > 0) {
        throw new IllegalStateException("cause a failure");
      }
      return true;
    });

    fail("Never get here - exception thrown");
  }
}
