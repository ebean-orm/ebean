package org.tests.query.other;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import org.ebeantest.LoggedSqlCollector;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Customer;
import org.tests.model.basic.Order;
import org.tests.model.basic.ResetBasicData;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestWhereAnnotation extends BaseTestCase {

  @Test
  public void fetchEager_inFirstQuery() {

    ResetBasicData.reset();

    LoggedSqlCollector.start();

    Ebean.find(Customer.class)
      .fetch("orders")
      .findList();

    List<String> loggedSql = LoggedSqlCollector.stop();

    assertEquals(1, loggedSql.size());

    String sql = loggedSql.get(0);
    assertTrue(sql.contains("t1.order_date is not null"));
  }

  @Test
  public void fetchLazy_inLazyLoadQuery() {

    ResetBasicData.reset();

    Ebean.getServerCacheManager().clearAll();

    LoggedSqlCollector.start();

    List<Customer> customers = Ebean.find(Customer.class)
      .order().asc("id")
      .findList();

    List<Order> orders = customers.get(0).getOrders();
    assertThat(orders.size()).isGreaterThan(0);
    orders.get(0).getStatus();

    List<String> loggedSql = LoggedSqlCollector.stop();

    assertThat(loggedSql).hasSize(2);
    assertThat(loggedSql.get(1)).contains("t0.order_date is not null");
  }

}
