package org.tests.query;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.Query;
import org.tests.model.basic.Order;
import org.tests.model.basic.ResetBasicData;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestQueryNullAssocOne extends BaseTestCase {

  @Test
  public void test() {

    ResetBasicData.reset();

    Query<Order> q0 = Ebean.find(Order.class).where().eq("customer", null).query();

    List<Order> orders = q0.findList();
    assertNotNull(orders);
    assertTrue(q0.getGeneratedSql().contains("where t0.kcustomer_id is null"));

    Query<Order> q1 = Ebean.find(Order.class).where().eq("customer.id", null).query();

    List<Order> o1 = q1.findList();
    assertTrue(o1.size() == orders.size());
    assertTrue(q1.getGeneratedSql().contains("where t0.kcustomer_id is null"));

    Query<Order> q2 = Ebean.find(Order.class).where().isNull("customer").query();

    List<Order> o2 = q2.findList();
    assertTrue(o2.size() == orders.size());
    assertTrue(q2.getGeneratedSql().contains("where t0.kcustomer_id is null"));

    Query<Order> q3 = Ebean.find(Order.class).where().isNull("customer").query();

    List<Order> o3 = q3.findList();
    assertTrue(o3.size() == orders.size());
    assertTrue(q3.getGeneratedSql().contains("where t0.kcustomer_id is null"));

  }
}
