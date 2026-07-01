package org.tests.basic;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import io.ebean.Query;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Order;
import org.tests.model.basic.ResetBasicData;

import java.sql.Timestamp;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestQueryWhereBetween extends BaseTestCase {

  long statCount;
  long statTotal;

  @Test
  public void testCountOrderBy() {

    ResetBasicData.reset();

    Timestamp t = new Timestamp(System.currentTimeMillis());

    Query<Order> query = DB.find(Order.class).setAutoTune(false).where()
      .betweenProperties("cretime", "updtime", t).orderBy().asc("orderDate").orderBy().desc("id");

    query.findList();

    String sql = query.getGeneratedSql();
    assertTrue(sql.contains("between t0.cretime and t0.updtime"));
  }

  @Disabled
  @Test
  public void doStuff() {

    someLoop(3, true);

    int loop = 30000;
    someLoop(loop, true);
    someLoop(loop, true);
    someLoop(loop, true);
    someLoop(loop);
    someLoop(loop);
    someLoop(loop);
    someLoop(loop);
    someLoop(loop);
    someLoop(loop);
    someLoop(loop);
    someLoop(loop);
    someLoop(loop);
    someLoop(loop);
    someLoop(loop);
    someLoop(loop);
    someLoop(loop);
    someLoop(loop);
    someLoop(loop);
    someLoop(loop);
    someLoop(loop);

    long avg = statTotal / statCount;
    System.out.println("avg "+avg);
  }

  private void someLoop(int loop) {
    someLoop(loop, false);
  }
  private void someLoop(int loop, boolean warm) {

    long start = System.currentTimeMillis();
    for (int i = 0; i < loop; i++) {
      someQuery();
    }

    long exe = System.currentTimeMillis() - start;
    System.out.println("exe: "+exe);

    if (!warm) {
      statTotal += exe;
      statCount++;
    }
  }

  private void someQuery() {

    Timestamp t = new Timestamp(System.currentTimeMillis());

    Query<Order> query = DB.find(Order.class).setAutoTune(false)
      .where()
      .le("cretime", t)
      .orderBy().asc("orderDate")
      .orderBy().desc("id");

    query.findList();

  }
}
