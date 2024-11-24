package org.tests.query;

import io.ebean.DB;
import io.ebean.PagedList;
import io.ebean.Transaction;
import io.ebean.test.LoggedSql;
import io.ebean.xtest.BaseTestCase;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Customer;
import org.tests.model.basic.Order;
import org.tests.model.basic.ResetBasicData;

import jakarta.persistence.PersistenceException;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class TestQueryFindPagedList extends BaseTestCase {

  @Test
  public void empty() {
    PagedList<Customer> empty = PagedList.emptyList();

    assertThat(empty.getList()).isEmpty();
    assertThat(empty.getTotalPageCount()).isEqualTo(0);
    assertThat(empty.getTotalCount()).isEqualTo(0);
    assertThat(empty.getPageIndex()).isEqualTo(0);
    assertThat(empty.getPageSize()).isEqualTo(0);
    assertThat(empty.hasNext()).isFalse();
    assertThat(empty.hasPrev()).isFalse();
    assertThat(empty.getDisplayXtoYofZ("a", "b")).isEqualTo("");
  }

  @Test
  public void test_noMaxRows() {
    assertThrows(PersistenceException.class, () ->
      DB.find(Order.class).findPagedList());
  }

  @Test
  public void test_maxRows_NoCount() {
    ResetBasicData.reset();

    PagedList<Order> pagedList = DB.find(Order.class)
      .setMaxRows(4)
      .findPagedList();

    LoggedSql.start();

    List<Order> orders = pagedList.getList();

    assertTrue(!orders.isEmpty());
    List<String> loggedSql = LoggedSql.stop();

    assertEquals(1, loggedSql.size());
  }


  @Test
  public void test_maxRows_countInBackground() throws ExecutionException, InterruptedException, TimeoutException {

    ResetBasicData.reset();

    PagedList<Order> pagedList = DB.find(Order.class)
      .setMaxRows(3)
      .findPagedList();

    pagedList.loadCount();
    List<Order> orders = pagedList.getList();

    // these are each getting the total row count
    int totalRowCount = pagedList.getTotalCount();
    Future<Integer> rowCount = pagedList.getFutureCount();
    Integer totalRowCountWithTimeout = rowCount.get(30, TimeUnit.SECONDS);
    Integer totalRowCountViaFuture = rowCount.get();

    assertTrue(orders.size() < totalRowCount);
    assertEquals(Integer.valueOf(totalRowCount), totalRowCountViaFuture);
    assertEquals(Integer.valueOf(totalRowCount), totalRowCountWithTimeout);
  }

  @Test
  public void test_maxRows_countInBackground_withLoadRowCount() {

    ResetBasicData.reset();

    // fetch less that total orders (page size 3)
    PagedList<Order> pagedList = DB.find(Order.class)
      .setMaxRows(3)
      .findPagedList();

    pagedList.loadCount();
    List<Order> orders = pagedList.getList();
    int totalRowCount = pagedList.getTotalCount();

    assertThat(orders.size()).isLessThan(totalRowCount);
    assertTrue(pagedList.hasNext());
    assertFalse(pagedList.hasPrev());


    // fetch less that total orders (page size 3)
    PagedList<Order> pagedList2 = DB.find(Order.class)
      .setFirstRow(1)
      .setMaxRows(3)
      .orderBy("id")
      .findPagedList();

    pagedList2.loadCount();
    List<Order> orders2 = pagedList2.getList();
    int totalRowCount2 = pagedList2.getTotalCount();
    assertTrue(pagedList2.hasNext());
    assertTrue(pagedList2.hasPrev());

    assertThat(totalRowCount).isEqualTo(totalRowCount2);
    assertThat(orders2.size()).isLessThan(totalRowCount);


    PagedList<Order> pagedList3 = DB.find(Order.class)
      .setFirstRow(2)
      .setMaxRows(150)
      .orderBy("id")
      .findPagedList();

    assertFalse(pagedList3.hasNext());
    assertTrue(pagedList3.hasPrev());

    List<Order> list3 = pagedList3.getList();
    String xtoYofZ = pagedList3.getDisplayXtoYofZ(" to ", " of ");
    assertThat(xtoYofZ).isEqualTo("3 to " + totalRowCount + " of " + totalRowCount);
    assertThat(list3.size()).isEqualTo(totalRowCount - 2);

    PagedList<Order> pagedList4 = DB.find(Order.class)
      .setFirstRow(0)
      .setMaxRows(totalRowCount)
      .findPagedList();

    assertFalse(pagedList4.hasNext());
    assertFalse(pagedList4.hasPrev());
    assertThat(pagedList4.getDisplayXtoYofZ(" to ", " of ")).isEqualTo("1 to " + totalRowCount + " of " + totalRowCount);

    PagedList<Order> pagedList5 = DB.find(Order.class)
      .setFirstRow(0)
      .setMaxRows(totalRowCount - 1)
      .findPagedList();

    assertTrue(pagedList5.hasNext());
    assertFalse(pagedList5.hasPrev());
  }

  @Test
  public void test_noCount() {

    ResetBasicData.reset();

    PagedList<Order> pagedList = DB.find(Order.class).setMaxRows(4).findPagedList();

    LoggedSql.start();

    List<Order> orders = pagedList.getList();

    assertTrue(!orders.isEmpty());
    List<String> loggedSql = LoggedSql.stop();

    assertEquals(1, loggedSql.size());
  }

  @Test
  public void test_countInBackground() throws ExecutionException, InterruptedException, TimeoutException {

    ResetBasicData.reset();

    PagedList<Order> pagedList = DB.find(Order.class).setMaxRows(3).findPagedList();

    Future<Integer> rowCount = pagedList.getFutureCount();
    List<Order> orders = pagedList.getList();

    // these are each getting the total row count
    int totalRowCount = pagedList.getTotalCount();
    Integer totalRowCountWithTimeout = rowCount.get(30, TimeUnit.SECONDS);
    Integer totalRowCountViaFuture = rowCount.get();

    assertTrue(orders.size() < totalRowCount);
    assertEquals(Integer.valueOf(totalRowCount), totalRowCountViaFuture);
    assertEquals(Integer.valueOf(totalRowCount), totalRowCountWithTimeout);
  }


  @Test
  public void test_countInBackground_withLoadRowCount() {

    ResetBasicData.reset();

    // fetch less that total orders (page size 3)
    PagedList<Order> pagedList = DB.find(Order.class).setMaxRows(3).findPagedList();

    pagedList.loadCount();
    List<Order> orders = pagedList.getList();
    int totalRowCount = pagedList.getTotalCount();

    assertThat(orders.size()).isLessThan(totalRowCount);
  }


  @Test
  public void test_countUsingForegound() {

    ResetBasicData.reset();

    PagedList<Order> pagedList = DB.find(Order.class).setMaxRows(3).findPagedList();

    LoggedSql.start();

    // kinda not normal but just wrap in a transaction to assert
    // the background fetch does not occur (which explicitly creates
    // its own transaction) ... so a bit naughty with the test here
    try (Transaction txn = DB.beginTransaction()) {

      List<Order> orders = pagedList.getList();
      int totalRowCount = pagedList.getTotalCount();

      // invoke it again but cached...
      int totalRowCountAgain = pagedList.getTotalCount();

      List<String> loggedSql = LoggedSql.stop();

      assertTrue(orders.size() < totalRowCount);
      assertEquals(2, loggedSql.size());
      assertEquals(totalRowCount, totalRowCountAgain);

      String firstTxn = loggedSql.get(0).substring(0, 10);
      String secTxn = loggedSql.get(1).substring(0, 10);

      assertEquals(firstTxn, secTxn);
    }
  }

  @Test
  public void test_usingAlias() {

    ResetBasicData.reset();

    PagedList<Order> pagedList = DB.find(Order.class)
      .alias("b")
      .where().raw("b.id > 0")
      .setMaxRows(6)
      .findPagedList();

    LoggedSql.start();

    pagedList.getTotalCount();
    pagedList.getList();

    List<String> loggedSql = LoggedSql.stop();

    assertEquals(2, loggedSql.size());
    assertThat(loggedSql.get(0)).contains("select count(*) from o_order b where b.id > 0");
    assertThat(trimSql(loggedSql.get(1), 3)).contains(" b.id, b.status, b.order_date");
  }

  @Test
  void test_forUpdate() {
    if (!isDb2()) {
      ResetBasicData.reset();

      try (Transaction txn = DB.beginTransaction()) {
        PagedList<Order> pagedList = DB.find(Order.class).forUpdate().setMaxRows(2).findPagedList();

        LoggedSql.start();
        int totalCount = pagedList.getTotalCount();
        assertThat(totalCount).isGreaterThan(2);

        List<Order> list = pagedList.getList();
        assertThat(list).hasSize(2);

        List<String> sql = LoggedSql.stop();
        assertThat(sql).hasSize(2);
        assertThat(sql.get(0)).contains("select count(*) from o_order t0;");
        if (isH2() || isPostgresCompatible()) {
          assertThat(sql.get(1)).contains(" limit 2 for update;");
        }
      }
    }
  }
}
