package org.tests.basic;

import io.ebean.annotation.Platform;
import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import io.ebean.Query;
import io.ebean.xtest.IgnorePlatform;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Order;
import org.tests.model.basic.ResetBasicData;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class TestLimitQuery extends BaseTestCase {

  @Test
  public void testLimitWithMany() {
    rob();
    rob();
  }

  @IgnorePlatform({Platform.MYSQL, Platform.MARIADB})
  @Test
  public void testMaxRowsZeroWithFirstRow() {

    ResetBasicData.reset();

    Query<Order> query = DB.find(Order.class)
      .setAutoTune(false)
      .fetch("details")
      .where().gt("details.id", 0)
      .setMaxRows(0)
      .setFirstRow(3)
      .orderBy().asc("orderDate");

    query.findList();

    String sql = query.getGeneratedSql();
    if (isH2()) {
      assertThat(sql).contains("offset 3");
      assertThat(sql).doesNotContain("limit");
    }
  }

  @Test
  public void testMaxRowsWithFirstRowZero() {

    ResetBasicData.reset();

    Query<Order> query = DB.find(Order.class)
      .setAutoTune(false)
      .fetch("details")
      .where().gt("details.id", 0)
      .setMaxRows(3)
      .setFirstRow(0).query();

    query.findList();

    String sql = query.getGeneratedSql();
    boolean hasLimit = sql.contains("limit 3");
    boolean hasOffset = sql.contains("offset");

    if (isH2()) {
      assertTrue(hasLimit);
      assertFalse(hasOffset);
    }
  }

  @Test
  public void testDefaults() {
    ResetBasicData.reset();

    Query<Order> query = DB.find(Order.class)
      .setAutoTune(false)
      .fetch("details")
      .where().gt("details.id", 0)
      .query();

    query.findList();

    String sql = query.getGeneratedSql();
    boolean hasLimit = sql.contains("limit");
    boolean hasOffset = sql.contains("offset");

    if (isH2()) {
      assertFalse(hasLimit);
      assertFalse(hasOffset);
    }
  }

  private void rob() {
    ResetBasicData.reset();

    Query<Order> query = DB.find(Order.class)
      .setAutoTune(false)
      .fetch("details")
      .where().gt("details.id", 0)
      .setMaxRows(10).query();
    //.findList();

    List<Order> list = query.findList();

    assertTrue(!list.isEmpty());

    String sql = query.getGeneratedSql();
    boolean hasDetailsJoin = sql.contains("join o_order_detail");
    boolean hasLimit = sql.contains("limit 10");
    boolean hasSelectedDetails = sql.contains("od.id,");
    boolean hasDistinct = sql.contains("select distinct");

    assertTrue(hasDetailsJoin);
    assertFalse(hasSelectedDetails);
    assertTrue(hasDistinct);
    if (isH2()) {
      assertTrue(hasLimit);
    }

    query = DB.find(Order.class)
      .setAutoTune(false)
      .fetch("details")
      .setMaxRows(10);

    query.findList();

    sql = query.getGeneratedSql();
    hasDetailsJoin = sql.contains("left join o_order_detail");
    hasLimit = sql.contains("limit 10");
    hasSelectedDetails = sql.contains("od.id");
    hasDistinct = sql.contains("select distinct");

    assertFalse(hasDetailsJoin);
    assertFalse(hasSelectedDetails);
    assertFalse(hasDistinct);
    if (isH2()) {
      assertTrue(hasLimit);
    }
  }
}
