package org.tests.query.orderby;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.OrderBy;
import io.ebean.Query;
import org.tests.model.basic.Order;
import org.tests.model.basic.ResetBasicData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestOrderByClear extends BaseTestCase {

  @Test
  public void test() {

    ResetBasicData.reset();

    Query<Order> query = DB.find(Order.class)
      .order().asc("orderDate");


    OrderBy<Order> orderBy = query.order();
    assertTrue(orderBy.containsProperty("orderDate"));

    orderBy.clear();
    assertFalse(orderBy.containsProperty("orderDate"));

    orderBy.asc("shipDate");
    assertTrue(orderBy.containsProperty("shipDate"));

    query.findList();
    String sql = query.getGeneratedSql();

    assertTrue(sql.contains("order by t0.ship_date"));

  }

}
