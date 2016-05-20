package com.avaje.tests.query.orderby;

import org.junit.Assert;
import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.Query;
import com.avaje.tests.model.basic.Customer;
import com.avaje.tests.model.basic.ResetBasicData;

public class TestOrderByWithFunction extends BaseTestCase {

  @Test
  public void testWithFunction() {

    if (isMsSqlServer()) return;

    ResetBasicData.reset();

    Query<Customer> query = Ebean.find(Customer.class).order("length(name),name");

    query.findList();
    String sql = query.getGeneratedSql();

    Assert.assertTrue(sql.contains("order by length(t0.name)"));
  }
}
