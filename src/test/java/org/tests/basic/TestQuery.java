package org.tests.basic;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.Query;
import org.tests.model.basic.Order;
import org.tests.model.basic.ResetBasicData;
import org.junit.Assert;
import org.junit.Test;

public class TestQuery extends BaseTestCase {

  @Test
  public void testCountOrderBy() {

    ResetBasicData.reset();

    Query<Order> query = Ebean.find(Order.class).setAutoTune(false).order().asc("orderDate")
      .order().desc("id");
    // .orderBy("orderDate");

    int rc = query.findList().size();
    // int rc = query.findRowCount();
    Assert.assertTrue(rc > 0);
    // String generatedSql = query.getGeneratedSql();
    // Assert.assertFalse(generatedSql.contains("order by"));

  }

  public void testForUpdate() {
    ResetBasicData.reset();

    Query<Order> query = Ebean.find(Order.class).setAutoTune(false).setForUpdate(false)
      .setMaxRows(1).order().asc("orderDate").order().desc("id");

    int rc = query.findList().size();
    Assert.assertTrue(rc > 0);
    Assert.assertTrue(!query.getGeneratedSql().toLowerCase().contains("for update"));

    query = Ebean.find(Order.class).setAutoTune(false).setForUpdate(true).setMaxRows(1).order()
      .asc("orderDate").order().desc("id");

    rc = query.findList().size();
    Assert.assertTrue(rc > 0);
    Assert.assertTrue(query.getGeneratedSql().toLowerCase().contains("for update"));
  }
}
