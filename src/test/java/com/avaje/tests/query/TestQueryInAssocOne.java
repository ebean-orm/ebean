package com.avaje.tests.query;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.Query;
import com.avaje.tests.model.basic.Customer;
import com.avaje.tests.model.basic.Order;
import com.avaje.tests.model.basic.ResetBasicData;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class TestQueryInAssocOne extends BaseTestCase {

  @Test
  public void test() {

    ResetBasicData.reset();

    List<Customer> list = Ebean.find(Customer.class).where().lt("id", 200).findList();

    Query<Order> query = Ebean.find(Order.class).where().in("customer", list).query();

    query.findList();
    String sql = query.getGeneratedSql();

    Assert.assertTrue(sql, sql.contains("join o_customer t1 on t1.id = t0.kcustomer_id"));
    Assert.assertTrue(sql, sql.contains("t0.kcustomer_id in (?"));

  }
}
