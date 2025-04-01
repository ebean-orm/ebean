package org.tests.query;

import io.ebean.FutureMap;
import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import io.ebean.FutureList;
import io.ebean.Transaction;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Order;
import org.tests.model.basic.Product;
import org.tests.model.basic.ResetBasicData;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class TestQueryFindFutureList extends BaseTestCase {

  @Test
  @SuppressWarnings("resource")
  void test_cancel() throws InterruptedException {
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
  void test_findFutureList() {
    ResetBasicData.reset();

    FutureList<Order> futureList = DB.find(Order.class).findFutureList();

    // wait for it to complete
    List<Order> orders = futureList.getUnchecked();

    assertEquals(DB.find(Order.class).findCount(), orders.size());
  }

  @Test
  void test_findFutureListWithTimeout() throws TimeoutException {
    ResetBasicData.reset();

    FutureList<Order> futureList = DB.find(Order.class).findFutureList();
    FutureList<Product> futureList2 = DB.find(Product.class).findFutureList();

    // wait for it to complete
    List<Order> orders = futureList.getUnchecked(1, TimeUnit.SECONDS);
    List<Product> products2 = futureList2.getUnchecked(1, TimeUnit.SECONDS);

    assertEquals(DB.find(Order.class).findCount(), orders.size());
    assertThat(products2).isNotEmpty();
  }

  @Test
  void test_findFutureMap_defaultIdAsKey() {
    ResetBasicData.reset();

    FutureMap<Integer, Order> futureMap = DB.find(Order.class)
      .findFutureMap();

    // wait for it to complete
    Map<Integer, Order> orders = futureMap.getUnchecked();

    assertEquals(DB.find(Order.class).findCount(), orders.size());
  }

  @Test
  void test_findFutureMap() {
    ResetBasicData.reset();

    FutureMap<String, Product> futureMap = DB.find(Product.class)
      .setMapKey("sku")
      .findFutureMap();

    // wait for it to complete
    Map<String, Product> products = futureMap.getUnchecked();
    Product desk = products.get("DSK1");
    assertThat(desk).isNotNull();
    assertThat(desk.getName()).isEqualTo("Desk");
    assertEquals(DB.find(Product.class).findCount(), products.size());
  }

}

