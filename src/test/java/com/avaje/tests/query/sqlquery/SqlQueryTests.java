package com.avaje.tests.query.sqlquery;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.QueryEachConsumer;
import com.avaje.ebean.QueryEachWhileConsumer;
import com.avaje.ebean.SqlQuery;
import com.avaje.ebean.SqlQueryListener;
import com.avaje.ebean.SqlRow;
import com.avaje.tests.model.basic.Order;
import com.avaje.tests.model.basic.ResetBasicData;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

public class SqlQueryTests {

  @Test
  public void setListener() {

    ResetBasicData.reset();

    int expectedRows = Ebean.find(Order.class).findRowCount();

    final AtomicInteger count = new AtomicInteger();

    SqlQuery sqlQuery = Ebean.createSqlQuery("select * from o_order");
    sqlQuery.setListener(new SqlQueryListener() {
      @Override
      public void process(SqlRow bean) {
        System.out.println("process row "+bean);
        count.incrementAndGet();
      }
    });
    // returns an empty list
    sqlQuery.findList();
    assertEquals(expectedRows, count.get());
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
