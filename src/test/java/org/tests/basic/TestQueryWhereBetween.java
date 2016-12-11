package org.tests.basic;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.Query;
import org.tests.model.basic.Order;
import org.tests.model.basic.ResetBasicData;
import org.junit.Assert;
import org.junit.Test;

import java.sql.Timestamp;

public class TestQueryWhereBetween extends BaseTestCase {

  @Test
  public void testCountOrderBy() {

    ResetBasicData.reset();

    Timestamp t = new Timestamp(System.currentTimeMillis());

    Query<Order> query = Ebean.find(Order.class).setAutoTune(false).where()
      .betweenProperties("cretime", "updtime", t).order().asc("orderDate").order().desc("id");

    query.findList();

    String sql = query.getGeneratedSql();
    Assert.assertTrue(sql.contains("between t0.cretime and t0.updtime"));
  }
}
