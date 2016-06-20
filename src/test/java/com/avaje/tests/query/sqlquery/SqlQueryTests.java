package com.avaje.tests.query.sqlquery;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.QueryEachConsumer;
import com.avaje.ebean.QueryEachWhileConsumer;
import com.avaje.ebean.SqlQuery;
import com.avaje.ebean.SqlRow;
import com.avaje.tests.model.basic.Order;
import com.avaje.tests.model.basic.ResetBasicData;
import org.avaje.ebeantest.LoggedSqlCollector;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

public class SqlQueryTests extends BaseTestCase {

  @Test
  public void firstRowMaxRows() {

    ResetBasicData.reset();

    SqlQuery sqlQuery = Ebean.createSqlQuery("Select * from o_order");
    sqlQuery.setFirstRow(3);
    sqlQuery.setMaxRows(10);

    LoggedSqlCollector.start();
    List<SqlRow> list = sqlQuery.findList();

    List<String> sql = LoggedSqlCollector.stop();

    assertThat(sql.get(0)).contains("Select * from o_order limit 10 offset 3; --bind()");
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

    assertThat(sql.get(0)).contains("Select * from o_order order by id limit 10");
  }

  @Test
  public void maxRows_withParam() {

    ResetBasicData.reset();

    SqlQuery sqlQuery = Ebean.createSqlQuery("select * from o_order where o_order.id > :id order by id ");
    sqlQuery.setParameter("id", 3);
    sqlQuery.setMaxRows(10);

    LoggedSqlCollector.start();
    sqlQuery.findList();
    List<String> sql = LoggedSqlCollector.stop();

    assertThat(sql.get(0)).contains("select * from o_order where o_order.id > ? order by id  limit 10;");
  }

  @Test
  public void findEachMaxRows() {

    ResetBasicData.reset();

    SqlQuery sqlQuery = Ebean.createSqlQuery("Select * from o_order");
    sqlQuery.setMaxRows(10);

    LoggedSqlCollector.start();
    sqlQuery.findEach(new QueryEachConsumer<SqlRow>() {
      @Override
      public void accept(SqlRow bean) {
        bean.get("id");
      }
    });
    List<String> sql = LoggedSqlCollector.stop();

    assertThat(sql.get(0)).contains("limit 10");
  }

  @Test
  public void findEach() {

    ResetBasicData.reset();

    int expectedRows = Ebean.find(Order.class).findRowCount();

    final AtomicInteger count = new AtomicInteger();

    SqlQuery sqlQuery = Ebean.createSqlQuery("select * from o_order");
    sqlQuery.findEach(new QueryEachConsumer<SqlRow>() {
      @Override
      public void accept(SqlRow bean) {
        count.incrementAndGet();
      }
    });

    assertEquals(expectedRows, count.get());
  }

  @Test
  public void findEachWhile() {

    ResetBasicData.reset();

    final AtomicInteger count = new AtomicInteger();

    SqlQuery sqlQuery = Ebean.createSqlQuery("select * from o_order order by id");
    sqlQuery.findEachWhile(new QueryEachWhileConsumer<SqlRow>() {
      @Override
      public boolean accept(SqlRow bean) {
        count.incrementAndGet();
        Integer id = bean.getInteger("id");
        return id.intValue() < 3;
      }
    });

    assertEquals(3, count.get());
  }

}
