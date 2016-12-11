package org.tests.basic;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.Query;
import org.tests.model.basic.Address;
import org.tests.model.basic.Customer;
import org.tests.model.basic.Order;
import org.tests.model.basic.ResetBasicData;
import org.junit.Assert;
import org.junit.Test;

public class TestAssocOneEqExpression extends BaseTestCase {

  @Test
  public void test() {

    ResetBasicData.reset();

    Customer c = new Customer();
    c.setId(1);

    Query<Order> query = Ebean.find(Order.class)
      .where().eq("customer", c)
      .query();

    query.findList();
    String sql = query.getGeneratedSql();
    Assert.assertTrue(sql.contains("where t0.kcustomer_id = ?"));

    Address b = new Address();
    b.setId((short) 1);

    Query<Order> q2 = Ebean.find(Order.class)
      .where().eq("customer.billingAddress", b)
      .query();

    q2.findList();
    sql = q2.getGeneratedSql();
    Assert.assertTrue(sql.contains("where t1.billing_address_id = ?"));

  }
}
