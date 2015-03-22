package com.avaje.tests.query;

import org.junit.Assert;
import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.Query;
import com.avaje.tests.model.basic.Customer;
import com.avaje.tests.model.basic.Order;
import com.avaje.tests.model.basic.ResetBasicData;

public class TestQueryExists extends BaseTestCase {

  @Test
  public void testExists() {
    ResetBasicData.reset();

    Query<Order> subQuery = Ebean.find(Order.class).alias("sq").select("id").where().raw("sq.kcustomer_id = qt.id").query();

    Query<Customer> query = Ebean.find(Customer.class).alias("qt").where().exists(subQuery).query();

    query.findList();
    String sql = query.getGeneratedSql();

    Assert.assertTrue(sql.indexOf("exists (") > 0);
  }

  @Test
  public void testNotExists() {
    ResetBasicData.reset();

    Query<Order> subQuery = Ebean.find(Order.class).alias("sq").select("id").where().raw("sq.kcustomer_id = qt.id").query();
    Query<Customer> query = Ebean.find(Customer.class).alias("qt").where().notExists(subQuery).query();

    query.findList();
    String sql = query.getGeneratedSql();

    Assert.assertTrue(sql.indexOf("not exists (") > 0);
  }
}
