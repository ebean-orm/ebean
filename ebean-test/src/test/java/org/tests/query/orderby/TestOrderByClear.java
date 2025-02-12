package org.tests.query.orderby;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import io.ebean.OrderBy;
import io.ebean.Query;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Order;
import org.tests.model.basic.OrderDetail;
import org.tests.model.basic.ResetBasicData;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestOrderByClear extends BaseTestCase {

  @Test
  public void test() {

    ResetBasicData.reset();

    Query<Order> query = DB.find(Order.class)
      .orderBy().asc("orderDate");


    OrderBy<Order> orderBy = query.orderBy();
    assertTrue(orderBy.containsProperty("orderDate"));

    orderBy.clear();
    assertFalse(orderBy.containsProperty("orderDate"));

    orderBy.asc("shipDate");
    assertTrue(orderBy.containsProperty("shipDate"));

    query.findList();
    String sql = query.getGeneratedSql();

    assertTrue(sql.contains("order by t0.ship_date"));

  }

  @Test
  public void testWithCache() {
    ResetBasicData.reset();

    Query<OrderDetail> query = DB.find(OrderDetail.class).where().idIn(1,2,3).query();
    query.setUseCache(true);
    query.findList();
    query.orderBy().asc("cretime").findList(); // hit cache
    query.orderBy().clear();
    query.findList(); // hit cache again

  }

}
