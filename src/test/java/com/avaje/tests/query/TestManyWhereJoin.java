package com.avaje.tests.query;

import org.junit.Assert;
import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.Query;
import com.avaje.tests.model.basic.Customer;
import com.avaje.tests.model.basic.Order;
import com.avaje.tests.model.basic.ResetBasicData;

public class TestManyWhereJoin extends BaseTestCase {

  @Test
  public void test() {

    ResetBasicData.reset();

    Query<Customer> query = Ebean.find(Customer.class).select("id,status")
    // .join("orders")
    // the where on a 'many' (like orders) requires an
    // additional join and distinct which is independent
    // of a fetch join (if there is a fetch join)
        .where().eq("orders.status", Order.Status.NEW)
        // .where().eq("orders.details.product.name", "Desk")
        .query();

    query.findList();
    String sql = query.getGeneratedSql();

    Assert.assertTrue(sql.indexOf("select distinct ") > -1);
    Assert.assertTrue(sql.indexOf("join o_order ") > -1);
    Assert.assertTrue(sql.indexOf(".status = ?") > -1);
  }
}
