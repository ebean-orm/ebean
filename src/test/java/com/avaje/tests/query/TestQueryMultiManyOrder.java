package com.avaje.tests.query;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.Query;
import com.avaje.tests.model.basic.Order;
import com.avaje.tests.model.basic.ResetBasicData;

public class TestQueryMultiManyOrder extends BaseTestCase {

  @Test
  public void test() {

    ResetBasicData.reset();

    Query<Order> q = Ebean.find(Order.class).fetch("shipments").fetch("details")
        .fetch("details.product").fetch("customer").where().gt("id", 0).query();

    List<Order> list = q.findList();
    String sql = q.getGeneratedSql();

    Assert.assertTrue(!list.isEmpty());
    Assert.assertTrue(sql.contains("join o_customer "));

    Assert.assertFalse(sql.contains("left outer join contact "));
    Assert.assertFalse(sql.contains("left outer join o_order_detail "));
    Assert.assertFalse(sql.contains("left outer join o_product "));

  }
}
