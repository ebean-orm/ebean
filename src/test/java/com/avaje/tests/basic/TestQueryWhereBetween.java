package com.avaje.tests.basic;

import java.sql.Timestamp;

import junit.framework.Assert;

import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.Query;
import com.avaje.tests.model.basic.Order;
import com.avaje.tests.model.basic.ResetBasicData;

public class TestQueryWhereBetween extends BaseTestCase {

  @Test
  public void testCountOrderBy() {

    ResetBasicData.reset();

    Timestamp t = new Timestamp(System.currentTimeMillis());

    Query<Order> query = Ebean.find(Order.class).setAutofetch(false).where()
        .betweenProperties("cretime", "updtime", t).order().asc("orderDate").order().desc("id");

    query.findList();

    String sql = query.getGeneratedSql();
    Assert.assertTrue(sql.indexOf("between t0.cretime and t0.updtime") > -1);
  }
}
