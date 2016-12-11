package org.tests.query.orderby;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.OrderBy;
import io.ebean.Query;
import org.tests.model.basic.Order;
import org.tests.model.basic.ResetBasicData;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class TestOrderByClear extends BaseTestCase {

  @Test
  public void test() {

    ResetBasicData.reset();

    Query<Order> query = Ebean.find(Order.class)
      .orderBy().asc("orderDate");


    OrderBy<Order> orderBy = query.orderBy();
    assertTrue(orderBy.containsProperty("orderDate"));

    orderBy.clear();
    Assert.assertFalse(orderBy.containsProperty("orderDate"));

    orderBy.asc("shipDate");
    assertTrue(orderBy.containsProperty("shipDate"));

    query.findList();
    String sql = query.getGeneratedSql();

    assertTrue(sql.contains("order by t0.ship_date"));

  }

}
