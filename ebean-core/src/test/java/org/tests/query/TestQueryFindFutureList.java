package org.tests.query;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.FutureList;
import io.ebean.Transaction;
import org.tests.model.basic.Order;
import org.tests.model.basic.ResetBasicData;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertEquals;

public class TestQueryFindFutureList extends BaseTestCase {

  @Test
  public void test_cancel() throws InterruptedException {

    ResetBasicData.reset();

    // warm the connection pool
    Transaction t0 = DB.createTransaction();
    Transaction t1 = DB.createTransaction();
    Transaction t2 = DB.createTransaction();
    t0.end();
    t1.end();
    t2.end();

    FutureList<Order> futureList = DB.find(Order.class).findFutureList();

    Thread.sleep(10);
    futureList.cancel(true);
    // calling again is ignored
    futureList.cancel(true);

    // don't shutdown immediately
    Thread.sleep(50);
  }

  @Test
  public void test_findFutureList() throws InterruptedException {

    ResetBasicData.reset();

    FutureList<Order> futureList = DB.find(Order.class).findFutureList();

    // wait for it to complete
    List<Order> orders = futureList.getUnchecked();

    assertEquals(DB.find(Order.class).findCount(), orders.size());
  }

  @Test
  public void test_findFutureListWithTimeout() throws InterruptedException, TimeoutException {

    ResetBasicData.reset();

    FutureList<Order> futureList = DB.find(Order.class).findFutureList();

    // wait for it to complete
    List<Order> orders = futureList.getUnchecked(1, TimeUnit.SECONDS);

    assertEquals(DB.find(Order.class).findCount(), orders.size());
  }

}

