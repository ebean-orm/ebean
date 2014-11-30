package com.avaje.tests.compositekeys;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.Query;
import com.avaje.tests.model.composite.ROrder;
import com.avaje.tests.model.composite.ROrderPK;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class TestCKeyIdInExpression extends BaseTestCase {

  @Test
  public void testDummy() {

  }

  // public void testRunManually() {
  //@Test
  public void notRanAutomatically() {

    EbeanServer server = CreateIdExpandedFormServer.create();

    ROrderPK k0 = new ROrderPK("compa", 100);
    ROrderPK k1 = new ROrderPK("compa", 101);
    ROrderPK k2 = new ROrderPK("b", 105);
    ROrderPK k3 = new ROrderPK("c", 106);

    List<ROrderPK> keys = new ArrayList<ROrderPK>();
    keys.add(k0);
    keys.add(k1);
    keys.add(k2);
    keys.add(k3);

    Query<ROrder> query = server.find(ROrder.class).where().idIn(keys).query();

    query.findList();
    String sql = query.getGeneratedSql();

    Assert.assertTrue(sql.contains("(r.company=? and r.order_number=?) or"));

    Query<ROrder> query2 = server.find(ROrder.class).setId(k0);

    query2.findUnique();
    sql = query2.getGeneratedSql();
    Assert.assertTrue(sql.contains("r.company = ? "));
    Assert.assertTrue(sql.contains(" and r.order_number = ?"));

    server.delete(ROrder.class, k0);

    server.delete(ROrder.class, keys);

  }

}
