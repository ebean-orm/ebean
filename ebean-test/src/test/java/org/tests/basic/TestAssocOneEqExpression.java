package org.tests.basic;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import io.ebean.Query;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Address;
import org.tests.model.basic.Customer;
import org.tests.model.basic.Order;
import org.tests.model.basic.ResetBasicData;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestAssocOneEqExpression extends BaseTestCase {

  @Test
  public void test() {

    ResetBasicData.reset();

    Customer c = new Customer();
    c.setId(1);

    Query<Order> query = DB.find(Order.class)
      .where().eq("customer", c)
      .query();

    query.findList();
    String sql = query.getGeneratedSql();
    assertTrue(sql.contains("where t0.kcustomer_id = ?"));

    Address b = new Address();
    b.setId(1);

    Query<Order> q2 = DB.find(Order.class)
      .where().eq("customer.billingAddress", b)
      .query();

    q2.findList();
    sql = q2.getGeneratedSql();
    assertTrue(sql.contains("where t1.billing_address_id = ?"));

  }
}
