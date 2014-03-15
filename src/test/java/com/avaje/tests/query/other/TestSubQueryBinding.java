package com.avaje.tests.query.other;

import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.Expression;
import com.avaje.ebean.Query;
import com.avaje.tests.model.basic.Customer;

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
