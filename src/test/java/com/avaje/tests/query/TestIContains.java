package com.avaje.tests.query;


import org.junit.Assert;
import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.Query;
import com.avaje.tests.model.basic.Customer;
import com.avaje.tests.model.basic.ResetBasicData;

public class TestIContains extends BaseTestCase {

  @Test
  public void testIContains() {

    ResetBasicData.reset();

    // case insensitive
    Query<Customer> query = Ebean.find(Customer.class).where().icontains("name", "Rob").query();

    query.findList();
    String generatedSql = query.getGeneratedSql();

    Assert.assertTrue(generatedSql.contains("lower(t0.name)"));

    // not case insensitive
    query = Ebean.find(Customer.class).where().contains("name", "Rob").query();

    query.findList();
    generatedSql = query.getGeneratedSql();

    Assert.assertTrue(generatedSql.contains(" t0.name "));

  }

}
