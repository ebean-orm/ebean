package io.ebean.xtest.internal.server.query;

import io.ebean.*;
import io.ebeaninternal.server.query.QueryFutureIds;
import io.ebeaninternal.server.query.QueryFutureList;
import io.ebeaninternal.server.query.QueryFutureRowCount;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Customer;
import org.tests.model.basic.ResetBasicData;

import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;

public class TestFutureRowCountErrorHandling extends BaseTestCase {

  @Test
  public void testFutureRowCount() throws InterruptedException {

    ResetBasicData.reset();

    Database server = DB.getDefault();

    Query<Customer> query = server.createQuery(Customer.class)
      .where().eq("doesNotExist", "this will fail")
      .query();

    FutureRowCount<Customer> futureRowCount = query.findFutureCount();

    QueryFutureRowCount<Customer> internalRowCount = (QueryFutureRowCount<Customer>) futureRowCount;
    Transaction t = internalRowCount.getTransaction();

    try {
      futureRowCount.get();
      fail("never get here as the SQL is invalid");

    } catch (ExecutionException e) {
      // Confirm the Transaction has been rolled back
      assertFalse(t.isActive());
    }

  }

  @Test
  public void testFutureIds() throws InterruptedException {

    ResetBasicData.reset();

    Database server = DB.getDefault();

    Query<Customer> query = server.createQuery(Customer.class)
      .where().eq("doesNotExist", "this will fail")
      .query();

    FutureIds<Customer> futureIds = query.findFutureIds();

    QueryFutureIds<Customer> internalFuture = (QueryFutureIds<Customer>) futureIds;
    Transaction t = internalFuture.getTransaction();

    try {
      internalFuture.get();
      fail("never get here as the SQL is invalid");

    } catch (ExecutionException e) {
      // Confirm the Transaction has been rolled back
      assertFalse(t.isActive());
    }

  }


  @Test
  public void testFutureList() throws InterruptedException {

    ResetBasicData.reset();

    Database server = DB.getDefault();

    Query<Customer> query = server.createQuery(Customer.class)
      .where().eq("doesNotExist", "this will fail")
      .query();

    FutureList<Customer> futureList = query.findFutureList();

    QueryFutureList<Customer> internalFuture = (QueryFutureList<Customer>) futureList;
    Transaction t = internalFuture.getTransaction();

    try {
      internalFuture.get();
      fail("never get here as the SQL is invalid");

    } catch (ExecutionException e) {
      // Confirm the Transaction has been rolled back
      assertFalse(t.isActive());
    }

  }
}
