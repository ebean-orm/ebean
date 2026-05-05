package org.tests.query;

import io.ebean.annotation.Platform;
import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import io.ebean.PagedList;
import io.ebean.test.LoggedSql;
import io.ebean.xtest.IgnorePlatform;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Order;
import org.tests.model.basic.ResetBasicData;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Ebean adds order by clause if none provided when using both first rows and max rows.
 */
public class TestAddOrderByWithFirstRowsMaxRows extends BaseTestCase {

  @IgnorePlatform({Platform.MYSQL, Platform.MARIADB})
  @Test
  public void test_firstRows() {

    ResetBasicData.reset();

    LoggedSql.start();

    DB.find(Order.class)
      .setFirstRow(3)
      .orderBy().asc("id")
      .findList();

    List<String> loggedSql = LoggedSql.stop();

    assertThat(loggedSql).hasSize(1);
    assertThat(loggedSql.get(0)).contains("order by t0.id");
  }


  @Test
  public void test_maxRows() {

    ResetBasicData.reset();

    LoggedSql.start();

    DB.find(Order.class)
      .setMaxRows(10)
      .findList();

    List<String> loggedSql = LoggedSql.stop();

    assertThat(loggedSql).hasSize(1);
    if (isH2()) {
      assertThat(loggedSql.get(0)).contains("from o_order t0 join o_customer t1 on t1.id = t0.kcustomer_id limit 10");
    }
  }


  @Test
  public void test_firstRowsMaxRows() {

    ResetBasicData.reset();

    LoggedSql.start();

    DB.find(Order.class)
      .setFirstRow(3)
      .setMaxRows(10)
      .orderBy().asc("id")
      .findList();

    List<String> loggedSql = LoggedSql.stop();

    assertThat(loggedSql).hasSize(1);
    assertThat(loggedSql.get(0)).contains("order by t0.id");
  }

  @Test
  public void test_maxRows1() {

    ResetBasicData.reset();

    LoggedSql.start();

    // maxRows 1 with no first rows means Ebean does not automatically
    // add the order by id to the query
    DB.find(Order.class)
      .setMaxRows(1)
      .findList();

    List<String> loggedSql = LoggedSql.stop();

    assertThat(loggedSql).hasSize(1);
    assertThat(loggedSql.get(0)).doesNotContain("order by t0.id");
  }

  @Test
  public void test_pagingOne() {

    ResetBasicData.reset();

    LoggedSql.start();

    PagedList<Order> pagedList =
      DB.find(Order.class)
        .setMaxRows(10)
        .findPagedList();

    pagedList.getList();

    List<String> loggedSql = LoggedSql.stop();

    assertThat(loggedSql).hasSize(1);
    if (isH2()) {
      assertThat(loggedSql.get(0)).contains("join o_customer t1 on t1.id = t0.kcustomer_id limit 10");
    }
  }

  @Test
  public void test_pagingTwo() {

    ResetBasicData.reset();

    LoggedSql.start();

    DB.find(Order.class)
      .setFirstRow(10)
      .setMaxRows(10)
      .orderBy("id")
      .findPagedList()
      .getList();

    List<String> loggedSql = LoggedSql.stop();

    assertThat(loggedSql).hasSize(1);
    if (isH2()) {
      assertThat(loggedSql.get(0)).contains(" limit 10 offset 10");
    }
  }


  @Test
  public void test_pagingAppendToExistingOrderBy() {

    ResetBasicData.reset();

    LoggedSql.start();

    DB.find(Order.class)
      .orderBy().asc("orderDate")
      .setMaxRows(10)
      .findPagedList()
      .getList();

    List<String> loggedSql = LoggedSql.stop();

    assertThat(loggedSql).hasSize(1);
    assertThat(loggedSql.get(0)).contains("order by t0.order_date");
  }

  @Test
  public void test_pagingExistingOrderByWithId() {

    ResetBasicData.reset();

    LoggedSql.start();

    DB.find(Order.class)
      .orderBy().asc("orderDate")
      .orderBy().desc("id")
      .setMaxRows(10)
      .findPagedList()
      .getList();

    List<String> loggedSql = LoggedSql.stop();

    assertThat(loggedSql).hasSize(1);
    assertThat(loggedSql.get(0)).contains("order by t0.order_date, t0.id desc");
  }
}
