package com.avaje.tests.query.other;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.tests.model.basic.Customer;
import com.avaje.tests.model.basic.Order;
import com.avaje.tests.model.basic.ResetBasicData;
import org.avaje.ebeantest.LoggedSqlCollector;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class TestWhereAnnotation extends BaseTestCase {

  @Test
  public void fetchEager_inFirstQuery() {

    ResetBasicData.reset();

    LoggedSqlCollector.start();

    Ebean.find(Customer.class)
      .fetch("orders")
      .findList();

    List<String> loggedSql = LoggedSqlCollector.stop();

    Assert.assertEquals(1, loggedSql.size());

    String sql = loggedSql.get(0);
    Assert.assertTrue(sql.contains("t1.order_date is not null"));
  }

  @Test
  public void fetchLazy_inLazyLoadQuery() {

    ResetBasicData.reset();

    LoggedSqlCollector.start();

    List<Customer> customers = Ebean.find(Customer.class).findList();

    List<Order> orders = customers.get(0).getOrders();
    orders.size();

    List<String> loggedSql = LoggedSqlCollector.stop();

    Assert.assertEquals(2, loggedSql.size());

    String sql = loggedSql.get(1);
    Assert.assertTrue(sql.contains("t0.order_date is not null"));
  }

}
