package org.tests.query.sqlquery;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.SqlQuery;
import io.ebean.SqlRow;
import io.ebean.meta.MetaTimedMetric;
import org.ebeantest.LoggedSqlCollector;
import org.junit.Test;
import org.tests.model.basic.Order;
import org.tests.model.basic.ResetBasicData;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

public class SqlQueryTests extends BaseTestCase {

  @Test
  public void newline_replacedInLogsOnly() {

    ResetBasicData.reset();

    String sql = "select * -- \n from o_customer";
    SqlQuery sqlQuery = Ebean.createSqlQuery(sql);

    List<SqlRow> list = sqlQuery.findList();
    assertThat(list).isNotEmpty();
  }

  @Test
  public void newLineLiteral_replacedInLogsOnly() {

    ResetBasicData.reset();

    String sql = "select 'hello\nthere' as hello from o_customer";
    SqlQuery sqlQuery = Ebean.createSqlQuery(sql);

    List<SqlRow> list = sqlQuery.findList();
    assertThat(list).isNotEmpty();

    assertThat(list.get(0).getString("hello")).isEqualTo("hello\nthere");
  }

  @Test
  public void firstRowMaxRows() {

    ResetBasicData.reset();

    SqlQuery sqlQuery = Ebean.createSqlQuery("Select * from o_order");
    sqlQuery.setFirstRow(3);
    sqlQuery.setMaxRows(10);

    LoggedSqlCollector.start();
    List<SqlRow> list = sqlQuery.findList();

    List<String> sql = LoggedSqlCollector.stop();

    if (isSqlServer()) {
      // FIXME: we should order by primary key ALWAYS (not by first column) when no
      // explicit order is specified. In postgres this leads to strange scrolling
      // artifacts.
      assertThat(sql.get(0)).contains("order by 1 offset 3 rows fetch next 10 rows only");
    } else if (isOracle()) {
      assertThat(sql.get(0)).contains("from o_order offset 3 rows fetch next 10 rows only");
    } else {
      assertThat(sql.get(0)).contains("Select * from o_order limit 10 offset 3; --bind()");
    }
    assertThat(list).isNotEmpty();
  }

  @Test
  public void firstRow() {

    if (isPostgres()) {

      ResetBasicData.reset();

      SqlQuery sqlQuery = Ebean.createSqlQuery("Select * from o_order order by id");
      sqlQuery.setFirstRow(3);

      LoggedSqlCollector.start();
      sqlQuery.findList();
      List<String> sql = LoggedSqlCollector.stop();

      assertThat(sql.get(0)).contains("Select * from o_order order by id offset 3");
    }
  }

  @Test
  public void maxRows() {

    ResetBasicData.reset();

    SqlQuery sqlQuery = Ebean.createSqlQuery("Select * from o_order order by id");
    sqlQuery.setMaxRows(10);

    LoggedSqlCollector.start();
    sqlQuery.findList();
    List<String> sql = LoggedSqlCollector.stop();

    if (isSqlServer()) {
      assertThat(sql.get(0)).contains("Select * from o_order order by id offset 0 rows fetch next 10 rows only;");
    } else if (isOracle()) {
      assertThat(sql.get(0)).contains("from o_order order by id fetch next 10 rows only;");
    } else {
      assertThat(sql.get(0)).contains("Select * from o_order order by id limit 10");
    }
  }

  @Test
  public void maxRows_withParam() {

    ResetBasicData.reset();

    resetAllMetrics();

    SqlQuery sqlQuery = Ebean.createSqlQuery("select * from o_order where o_order.id > :id order by id")
      .setParameter("id", 3)
      .setMaxRows(10)
      .setLabel("findList-3-10");


    LoggedSqlCollector.start();
    sqlQuery.findList();
    List<String> sql = LoggedSqlCollector.stop();

    if (isSqlServer()) {
      assertThat(sql.get(0)).contains("select * from o_order where o_order.id > ? order by id offset 0 rows fetch next 10 rows only;");
    } else if (isOracle()) {
      assertThat(sql.get(0)).contains("order by id fetch next 10 rows only");
    } else {
      assertThat(sql.get(0)).contains("select * from o_order where o_order.id > ? order by id limit 10;");
    }

    assertThat(sqlMetrics()).isNotEmpty();
  }

  @Test
  public void findEachMaxRows() {

    ResetBasicData.reset();

    resetAllMetrics();

    SqlQuery sqlQuery = Ebean.createSqlQuery("Select * from o_order")
      .setMaxRows(10)
      .setLabel("findEach-Max10Rows");

    LoggedSqlCollector.start();
    sqlQuery.findEach(bean -> bean.get("id"));
    List<String> sql = LoggedSqlCollector.stop();

    if (isSqlServer()) {
      assertThat(sql.get(0)).contains("offset 0 rows fetch next 10 rows only");
    } else if (isOracle()) {
      assertThat(sql.get(0)).contains("fetch next 10 rows only");
    } else {
      assertThat(sql.get(0)).contains("limit 10");
    }

    List<MetaTimedMetric> sqlMetrics = sqlMetrics();
    assertThat(sqlMetrics).hasSize(1);
    assertThat(sqlMetrics.get(0).getName()).isEqualTo("sql.query.findEach-Max10Rows");
  }

  @Test
  public void findEach() {

    ResetBasicData.reset();

    int expectedRows = Ebean.find(Order.class).findCount();

    final AtomicInteger count = new AtomicInteger();

    SqlQuery sqlQuery = Ebean.createSqlQuery("select * from o_order");
    sqlQuery.findEach(bean -> count.incrementAndGet());

    assertEquals(expectedRows, count.get());
  }

  @Test
  public void findEachWhile() {

    ResetBasicData.reset();

    final AtomicInteger count = new AtomicInteger();

    SqlQuery sqlQuery = Ebean.createSqlQuery("select * from o_order order by id");
    sqlQuery.findEachWhile(bean -> {
      count.incrementAndGet();
      Integer id = bean.getInteger("id");
      return id < 3;
    });

    assertEquals(3, count.get());
  }

}
