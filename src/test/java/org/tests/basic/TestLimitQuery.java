package org.tests.basic;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.Query;
import org.tests.model.basic.Order;
import org.tests.model.basic.ResetBasicData;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


public class TestLimitQuery extends BaseTestCase {

  @Test
  public void testLimitWithMany() {
    rob();
    rob();
  }

  @Test
  public void testMaxRowsZeroWithFirstRow() {

    ResetBasicData.reset();

    Query<Order> query = Ebean.find(Order.class)
      .setAutoTune(false)
      .fetch("details")
      .where().gt("details.id", 0)
      .setMaxRows(0)
      .setFirstRow(3)
      .order().asc("orderDate");

    query.findList();

    String sql = query.getGeneratedSql();
    if (isH2()) {
      assertThat(sql).contains("offset 3");
      assertThat(sql).contains("limit 0");
    }
  }

  @Test
  public void testMaxRowsWithFirstRowZero() {

    ResetBasicData.reset();

    Query<Order> query = Ebean.find(Order.class)
      .setAutoTune(false)
      .fetch("details")
      .where().gt("details.id", 0)
      .setMaxRows(3)
      .setFirstRow(0);

    query.findList();

    String sql = query.getGeneratedSql();
    boolean hasLimit = sql.contains("limit 3");
    boolean hasOffset = sql.contains("offset");

    if (isH2()) {
      Assert.assertTrue(sql, hasLimit);
      Assert.assertFalse(sql, hasOffset);
    }
  }

  @Test
  public void testDefaults() {
    ResetBasicData.reset();

    Query<Order> query = Ebean.find(Order.class)
      .setAutoTune(false)
      .fetch("details")
      .where().gt("details.id", 0)
      .query();

    query.findList();

    String sql = query.getGeneratedSql();
    boolean hasLimit = sql.contains("limit");
    boolean hasOffset = sql.contains("offset");

    if (isH2()) {
      Assert.assertFalse(hasLimit);
      Assert.assertFalse(hasOffset);
    }
  }

  private void rob() {
    ResetBasicData.reset();

    Query<Order> query = Ebean.find(Order.class)
      .setAutoTune(false)
      .fetch("details")
      .where().gt("details.id", 0)
      .setMaxRows(10);
    //.findList();

    List<Order> list = query.findList();

    Assert.assertTrue("sz > 0", !list.isEmpty());

    String sql = query.getGeneratedSql();
    boolean hasDetailsJoin = sql.contains("join o_order_detail");
    boolean hasLimit = sql.contains("limit 10");
    boolean hasSelectedDetails = sql.contains("od.id,");
    boolean hasDistinct = sql.contains("select distinct");

    Assert.assertTrue(hasDetailsJoin);
    Assert.assertFalse(hasSelectedDetails);
    Assert.assertTrue(hasDistinct);
    if (isH2()) {
      Assert.assertTrue(hasLimit);
    }

    query = Ebean.find(Order.class)
      .setAutoTune(false)
      .fetch("details")
      .setMaxRows(10);

    query.findList();

    sql = query.getGeneratedSql();
    hasDetailsJoin = sql.contains("left join o_order_detail");
    hasLimit = sql.contains("limit 10");
    hasSelectedDetails = sql.contains("od.id");
    hasDistinct = sql.contains("select distinct");

    Assert.assertFalse("no join with maxRows", hasDetailsJoin);
    Assert.assertFalse(hasSelectedDetails);
    Assert.assertFalse(hasDistinct);
    if (isH2()) {
      Assert.assertTrue(hasLimit);
    }
  }
}
