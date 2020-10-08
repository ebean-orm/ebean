package org.tests.query.other;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.EbeanServer;
import io.ebean.Expression;
import io.ebean.Query;
import org.tests.model.basic.Customer;
import org.junit.Test;

public class TestSubQueryBinding extends BaseTestCase {

  @Test
  public void test() {

    EbeanServer server = Ebean.getServer(null);

    Query<Customer> someCustomerIds = server.find(Customer.class).select("id").where().lt("id", 5).query();

    Expression someCustIdsExpression = server.getExpressionFactory().in("id", someCustomerIds);

    Query<Customer> query = server.find(Customer.class).where().like("name", "Rob%").not(someCustIdsExpression).query();

    query.findList();

  }

}
