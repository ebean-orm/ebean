package com.avaje.tests.query;

import org.junit.Assert;
import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.Query;
import com.avaje.tests.model.basic.Customer;
import com.avaje.tests.model.basic.ResetBasicData;

public class TestExprNestedDisjunction extends BaseTestCase {

  @Test
  public void test() {

    ResetBasicData.reset();

    // java.sql.Date dateAfter = java.sql.Date.valueOf("2010-01-01");

    java.sql.Date onAfter = java.sql.Date.valueOf("2009-08-31");

    Query<Customer> q = Ebean.find(Customer.class).where().disjunction()
        .conjunction().startsWith("name", "r").eq("anniversary", onAfter).endJunction()
        .conjunction().eq("status", Customer.Status.ACTIVE).gt("id", 0).endJunction().orderBy()
        .asc("name");

    q.findList();
    String s = q.getGeneratedSql();

    Assert.assertTrue(s
        .contains("(t0.name like ?  and t0.anniversary = ? )  or (t0.status = ?  and t0.id > ? )"));
  }

}
