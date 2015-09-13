package com.avaje.tests.query;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.PagedList;
import com.avaje.tests.model.basic.Order;
import com.avaje.tests.model.basic.ResetBasicData;
import org.avaje.ebeantest.LoggedSqlCollector;
import org.junit.Test;

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
        .findPagedList(0, 10);

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
        .findPagedList(1, 10)
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
        .findPagedList(0, 10)
        .getList();

    List<String> loggedSql = LoggedSqlCollector.stop();

    assertThat(loggedSql).hasSize(1);
    assertThat(loggedSql.get(0)).contains("order by t0.order_date, t0.id");
  }

  @Test
  public void test_pagingExistingOrderByWithId() {

    ResetBasicData.reset();

    LoggedSqlCollector.start();

    Ebean.find(Order.class)
        .order().asc("orderDate")
        .order().desc("id")
        .findPagedList(0, 10)
        .getList();

    List<String> loggedSql = LoggedSqlCollector.stop();

    assertThat(loggedSql).hasSize(1);
    assertThat(loggedSql.get(0)).contains("order by t0.order_date, t0.id desc");
  }
}
