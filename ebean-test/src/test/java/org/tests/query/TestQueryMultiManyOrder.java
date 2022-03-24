package org.tests.query;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import io.ebean.Query;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Order;
import org.tests.model.basic.ResetBasicData;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestQueryMultiManyOrder extends BaseTestCase {

  @Test
  public void test() {

    ResetBasicData.reset();

    Query<Order> q = DB.find(Order.class).fetch("shipments").fetch("details")
      .fetch("details.product").fetch("customer").where().gt("id", 0).query();

    List<Order> list = q.findList();
    String sql = q.getGeneratedSql();

    assertTrue(!list.isEmpty());
    assertTrue(sql.contains("join o_customer "));

    assertFalse(sql.contains("left join contact "));
    assertFalse(sql.contains("left join o_order_detail "));
    assertFalse(sql.contains("left join o_product "));

  }
}
