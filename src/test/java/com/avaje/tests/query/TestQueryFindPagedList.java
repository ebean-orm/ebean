package com.avaje.tests.query;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.PagedList;
import com.avaje.tests.model.basic.Order;
import com.avaje.tests.model.basic.ResetBasicData;
import org.avaje.ebeantest.LoggedSqlCollector;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class TestQueryFindPagedList extends BaseTestCase {

  @Test
  public void test_noCount() throws ExecutionException, InterruptedException {

    ResetBasicData.reset();

    PagedList<Order> pagedList = Ebean.find(Order.class).findPagedList(0, 4);

    LoggedSqlCollector.start();

    List<Order> orders = pagedList.getList();

    assertTrue(!orders.isEmpty());
    List<String> loggedSql = LoggedSqlCollector.stop();

    assertEquals("Only 1 SQL statement, no count query",1, loggedSql.size());
  }

  @Test
  public void test_countInBackground() throws ExecutionException, InterruptedException, TimeoutException {

    ResetBasicData.reset();

    PagedList<Order> pagedList = Ebean.find(Order.class).findPagedList(0, 3);

    LoggedSqlCollector.start();

    Future<Integer> rowCount = pagedList.getFutureRowCount();
    List<Order> orders = pagedList.getList();

    // these are each getting the total row count
    int totalRowCount = pagedList.getTotalRowCount();
    Integer totalRowCountWithTimeout = rowCount.get(30, TimeUnit.SECONDS);
    Integer totalRowCountViaFuture = rowCount.get();

    List<String> loggedSql = LoggedSqlCollector.stop();

    assertTrue(orders.size() < totalRowCount);
    assertEquals(Integer.valueOf(totalRowCount), totalRowCountViaFuture);
    assertEquals(Integer.valueOf(totalRowCount), totalRowCountWithTimeout);
    assertEquals(2, loggedSql.size());

    String firstTxn = loggedSql.get(0).substring(0, 10);
    String secTxn = loggedSql.get(1).substring(0, 10);

    assertNotEquals(firstTxn, secTxn);
  }


  @Test
  public void test_countInBackground_withLoadRowCount() {

    ResetBasicData.reset();

    PagedList<Order> pagedList = Ebean.find(Order.class).findPagedList(0, 5);

    LoggedSqlCollector.start();

    pagedList.loadRowCount();
    List<Order> orders = pagedList.getList();
    int totalRowCount = pagedList.getTotalRowCount();

    List<String> loggedSql = LoggedSqlCollector.stop();

    assertTrue(orders.size() < totalRowCount);
    assertEquals("loggedSql: "+loggedSql, 2, loggedSql.size());

    String firstTxn = loggedSql.get(0).substring(0, 10);
    String secTxn = loggedSql.get(1).substring(0, 10);

    assertNotEquals(firstTxn, secTxn);
  }


  @Test
  public void test_countUsingForegound() throws ExecutionException, InterruptedException {

    ResetBasicData.reset();

    PagedList<Order> pagedList = Ebean.find(Order.class).findPagedList(0, 6);

    LoggedSqlCollector.start();

    // kinda not normal but just wrap in a transaction to assert
    // the background fetch does not occur (which explicitly creates
    // its own transaction) ... so a bit naughty with the test here
    Ebean.beginTransaction();
    try {

      List<Order> orders = pagedList.getList();
      int totalRowCount = pagedList.getTotalRowCount();

      // invoke it again but cached...
      int totalRowCountAgain = pagedList.getTotalRowCount();

      List<String> loggedSql = LoggedSqlCollector.stop();

      assertTrue(orders.size() < totalRowCount);
      assertEquals(2, loggedSql.size());
      assertEquals(totalRowCount, totalRowCountAgain);

      String firstTxn = loggedSql.get(0).substring(0, 10);
      String secTxn = loggedSql.get(1).substring(0, 10);

      assertEquals(firstTxn, secTxn);

    } finally {
      Ebean.endTransaction();
    }
  }
}
