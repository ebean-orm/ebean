package org.tests.query;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import io.ebean.Query;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Order;
import org.tests.model.basic.ResetBasicData;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

public class TestQueryPlanCacheRowCount extends BaseTestCase {

  @Test
  public void test() {

    ResetBasicData.reset();

    Query<Order> query = DB.find(Order.class).where().eq("status", Order.Status.NEW).ge("id", 1)
      .orderBy().desc("id");

    int rc0 = query.findCount();

    List<Integer> ids = query.findIds();
    assertEquals(rc0, ids.size());

    List<Order> list0 = query.findList();
    assertEquals(rc0, list0.size());

    int rc1 = query.findCount();
    assertEquals(rc0, rc1);

    List<Integer> ids1 = query.findIds();
    assertEquals(rc0, ids1.size());

    List<Order> list1 = query.findList();
    assertEquals(rc0, list1.size());

    int idGt = 5;
    if (!ids1.isEmpty()) {
      Object id = ids.get(0);
      idGt = Integer.valueOf("" + id);
    }

    // should still hit query plan cache
    Query<Order> query2 = DB.find(Order.class).where().eq("status", Order.Status.NEW)
      .ge("id", idGt).orderBy().desc("id");

    int rc2 = query2.findCount();

    System.out.println("Expection Not same " + rc0 + " != " + rc2);
    assertNotSame(rc0, rc2);

    List<Integer> ids2 = query2.findIds();
    assertEquals(rc2, ids2.size());

    List<Order> list2 = query2.findList();
    assertEquals(rc2, list2.size());

  }

}
