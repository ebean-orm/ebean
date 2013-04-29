package com.avaje.tests.query;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.Expr;
import com.avaje.ebean.Junction;
import com.avaje.ebean.Query;
import com.avaje.tests.model.basic.Customer;
import com.avaje.tests.model.basic.ResetBasicData;

public class TestLimitQuery extends BaseTestCase {

  @Test
  public void testHasManyWithLimit() {
    ResetBasicData.reset();

    Query<Customer> query = Ebean.find(Customer.class);
    query.setAutofetch(false);
    query.setFirstRow(0);
    query.setMaxRows(10);

    Junction<Customer> junc = Expr.disjunction(query);
    junc.add(Expr.like("name", "%A%"));
    query.where(junc);

    List<Customer> customer = query.findList();
    Assert.assertTrue(customer.size() > 0); // should at least find the
                                     // "Cust NoAddress" customer
  }
}