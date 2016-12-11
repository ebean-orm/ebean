package io.ebeaninternal.server.query;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.EbeanServer;
import io.ebean.FutureIds;
import io.ebean.FutureList;
import io.ebean.FutureRowCount;
import io.ebean.Query;
import io.ebean.Transaction;
import org.tests.model.basic.Customer;
import org.tests.model.basic.ResetBasicData;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.ExecutionException;

public class TestFutureRowCountErrorHandling extends BaseTestCase {

  @Test
  public void testFutureRowCount() throws InterruptedException {

    ResetBasicData.reset();

    EbeanServer server = Ebean.getServer(null);

    Query<Customer> query = server.createQuery(Customer.class)
      .where().eq("doesNotExist", "this will fail")
      .query();

    FutureRowCount<Customer> futureRowCount = server.findFutureCount(query, null);

    QueryFutureRowCount<Customer> internalRowCount = (QueryFutureRowCount<Customer>) futureRowCount;
    Transaction t = internalRowCount.getTransaction();

    try {
      futureRowCount.get();
      Assert.assertTrue("never get here as the SQL is invalid", false);

    } catch (ExecutionException e) {
      // Confirm the Transaction has been rolled back
      Assert.assertFalse("Underlying transaction was rolled back cleanly", t.isActive());
    }

  }

  @Test
  public void testFutureIds() throws InterruptedException {

    ResetBasicData.reset();

    EbeanServer server = Ebean.getServer(null);

    Query<Customer> query = server.createQuery(Customer.class)
      .where().eq("doesNotExist", "this will fail")
      .query();

    FutureIds<Customer> futureIds = server.findFutureIds(query, null);

    QueryFutureIds<Customer> internalFuture = (QueryFutureIds<Customer>) futureIds;
    Transaction t = internalFuture.getTransaction();

    try {
      internalFuture.get();
      Assert.assertTrue("never get here as the SQL is invalid", false);

    } catch (ExecutionException e) {
      // Confirm the Transaction has been rolled back
      Assert.assertFalse("Underlying transaction was rolled back cleanly", t.isActive());
    }

  }


  @Test
  public void testFutureList() throws InterruptedException {

    ResetBasicData.reset();

    EbeanServer server = Ebean.getServer(null);

    Query<Customer> query = server.createQuery(Customer.class)
      .where().eq("doesNotExist", "this will fail")
      .query();

    FutureList<Customer> futureList = server.findFutureList(query, null);

    QueryFutureList<Customer> internalFuture = (QueryFutureList<Customer>) futureList;
    Transaction t = internalFuture.getTransaction();

    try {
      internalFuture.get();
      Assert.assertTrue("never get here as the SQL is invalid", false);

    } catch (ExecutionException e) {
      // Confirm the Transaction has been rolled back
      Assert.assertFalse("Underlying transaction was rolled back cleanly", t.isActive());
    }

  }
}
