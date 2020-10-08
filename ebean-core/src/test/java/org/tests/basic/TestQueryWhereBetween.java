package org.tests.basic;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.Query;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.tests.model.basic.Order;
import org.tests.model.basic.ResetBasicData;

import java.sql.Timestamp;

public class TestQueryWhereBetween extends BaseTestCase {

  long statCount;
  long statTotal;

  @Test
  public void testCountOrderBy() {

    ResetBasicData.reset();

    Timestamp t = new Timestamp(System.currentTimeMillis());

    Query<Order> query = Ebean.find(Order.class).setAutoTune(false).where()
      .betweenProperties("cretime", "updtime", t).order().asc("orderDate").order().desc("id");

    query.findList();

    String sql = query.getGeneratedSql();
    Assert.assertTrue(sql.contains("between t0.cretime and t0.updtime"));
  }

  @Ignore
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

    Query<Order> query = Ebean.find(Order.class).setAutoTune(false)
      .where()
      .le("cretime", t)
      .order().asc("orderDate")
      .order().desc("id");

    query.findList();

  }
}
