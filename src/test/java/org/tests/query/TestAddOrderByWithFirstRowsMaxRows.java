package org.tests.query;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.PagedList;
import org.ebeantest.LoggedSqlCollector;
import org.junit.Test;
import org.tests.model.basic.Order;
import org.tests.model.basic.ResetBasicData;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Ebean adds order by clause if none provided when using both first rows and max rows.
 */
public class TestAddOrderByWithFirstRowsMaxRows extends BaseTestCase {

  @Test
  public void test_firstRows() {

    ResetBasicData.reset();

    LoggedSqlCollector.start();

    Ebean.find(Order.class)
      .setFirstRow(3)
      .orderBy().asc("id")
      .findList();

    List<String> loggedSql = LoggedSqlCollector.stop();

    assertThat(loggedSql).hasSize(1);
    assertThat(loggedSql.get(0)).contains("order by t0.id");
  }


  @Test
  public void test_maxRows() {

    ResetBasicData.reset();

    LoggedSqlCollector.start();

    Ebean.find(Order.class)
      .setMaxRows(10)
      .findList();

    List<String> loggedSql = LoggedSqlCollector.stop();

    assertThat(loggedSql).hasSize(1);
    assertThat(loggedSql.get(0)).contains("order by t0.id");
  }


  @Test
  public void test_firstRowsMaxRows() {

    ResetBasicData.reset();

    LoggedSqlCollector.start();

    Ebean.find(Order.class)
      .setFirstRow(3)
      .setMaxRows(10)
      .orderBy().asc("id")
      .findList();

    List<String> loggedSql = LoggedSqlCollector.stop();

    assertThat(loggedSql).hasSize(1);
    assertThat(loggedSql.get(0)).contains("order by t0.id");
  }

  @Test
  public void test_maxRows1() {

    ResetBasicData.reset();

    LoggedSqlCollector.start();

    // maxRows 1 with no first rows means Ebean does not automatically
    // add the order by id to the query
    Ebean.find(Order.class)
      .setMaxRows(1)
      .findList();

    List<String> loggedSql = LoggedSqlCollector.stop();

    assertThat(loggedSql).hasSize(1);
    assertThat(loggedSql.get(0)).doesNotContain("order by t0.id");
  }

  @Test
  public void test_pagingOne() {

    ResetBasicData.reset();

    LoggedSqlCollector.start();

    PagedList<Order> pagedList =
      Ebean.find(Order.class)
        .setMaxRows(10)
        .findPagedList();

    pagedList.getList();

    List<String> loggedSql = LoggedSqlCollector.stop();

    assertThat(loggedSql).hasSize(1);
    assertThat(loggedSql.get(0)).contains("order by t0.id");
  }

  @Test
  public void test_pagingTwo() {

    ResetBasicData.reset();

    LoggedSqlCollector.start();

    Ebean.find(Order.class)
      .setFirstRow(10)
      .setMaxRows(10)
      .findPagedList()
      .getList();

    List<String> loggedSql = LoggedSqlCollector.stop();

    assertThat(loggedSql).hasSize(1);
    assertThat(loggedSql.get(0)).contains("order by t0.id");
  }


  @Test
  public void test_pagingAppendToExistingOrderBy() {

    ResetBasicData.reset();

    LoggedSqlCollector.start();

    Ebean.find(Order.class)
      .order().asc("orderDate")
      .setMaxRows(10)
      .findPagedList()
      .getList();

    List<String> loggedSql = LoggedSqlCollector.stop();

    assertThat(loggedSql).hasSize(1);
    assertThat(loggedSql.get(0)).contains("order by t0.order_date");
  }

  @Test
  public void test_pagingExistingOrderByWithId() {

    ResetBasicData.reset();

    LoggedSqlCollector.start();

    Ebean.find(Order.class)
      .order().asc("orderDate")
      .order().desc("id")
      .setMaxRows(10)
      .findPagedList()
      .getList();

    List<String> loggedSql = LoggedSqlCollector.stop();

    assertThat(loggedSql).hasSize(1);
    assertThat(loggedSql.get(0)).contains("order by t0.order_date, t0.id desc");
  }
}
