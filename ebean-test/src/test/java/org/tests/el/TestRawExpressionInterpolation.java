package org.tests.el;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import io.ebean.Query;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Customer;

class TestRawExpressionInterpolation extends BaseTestCase {

  @Test
  void test() {

    Query<Customer> query = DB.find(Customer.class)
      .where().raw("name like ?", "Rob%")
      .query();

    query.findList();

    assertSql(query).contains("where t0.name like ?");

    Query<Customer> query2 = DB.find(Customer.class)
      .where().raw("? like concat('%', name, '%')", "Rob")
      .query();

    query2.findList();

    assertSql(query2).contains("where ? like concat('%', t0.name, '%')");
  }

}
