package com.avaje.tests.query;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.FetchConfig;
import com.avaje.tests.model.basic.Customer;
import com.avaje.tests.model.basic.Order;
import com.avaje.tests.model.basic.ResetBasicData;
import org.avaje.ebeantest.LoggedSqlCollector;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestQueryFilterMany extends BaseTestCase {

  @Test
  public void test() {

    ResetBasicData.reset();

    LoggedSqlCollector.start();

    Customer customer = Ebean.find(Customer.class)
            .fetch("orders", new FetchConfig().lazy())
            .filterMany("orders").eq("status", Order.Status.NEW)
            .where().ieq("name", "Rob")
            .order().asc("id").setMaxRows(1)
            .findList().get(0);

    customer.getOrders().size();

    List<String> sqlList = LoggedSqlCollector.stop();
    assertEquals(2, sqlList.size());
    assertTrue(sqlList.get(1).contains("status = ?"));

    // Currently this does not include the query filter
    Ebean.refreshMany(customer, "orders");

  }
}
