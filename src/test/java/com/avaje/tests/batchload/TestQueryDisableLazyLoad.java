package com.avaje.tests.batchload;

import com.avaje.ebean.Ebean;
import com.avaje.tests.model.basic.Order;
import com.avaje.tests.model.basic.OrderDetail;
import com.avaje.tests.model.basic.ResetBasicData;
import org.avaje.ebeantest.LoggedSqlCollector;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

public class TestQueryDisableLazyLoad {

  @Test
  public void onAssocMany() {

    ResetBasicData.reset();

    LoggedSqlCollector.start();

    List<Order> l0 = Ebean.find(Order.class)
        .setDisableLazyLoading(true)
        .order().asc("id")
        .findList();

    assertThat(l0).isNotEmpty();

    Order order = l0.get(0);

    List<OrderDetail> details = order.getDetails();
    assertEquals(details.size(), 0);

    List<String> loggedSql = LoggedSqlCollector.stop();
    assertThat(loggedSql).hasSize(1);

    assertThat(loggedSql.get(0)).contains("select t0.id c0, t0.status c1, t0.order_date c2,");
    assertThat(loggedSql.get(0)).contains(" from o_order t0 ");
  }
}
