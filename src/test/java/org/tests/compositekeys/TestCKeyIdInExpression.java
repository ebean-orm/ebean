package org.tests.compositekeys;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.EbeanServer;
import io.ebean.Query;
import org.tests.model.composite.RCustomer;
import org.tests.model.composite.RCustomerKey;
import org.tests.model.composite.ROrder;
import org.tests.model.composite.ROrderPK;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class TestCKeyIdInExpression extends BaseTestCase {

  @Test
  public void testDummy() {

  }

  //@Test
  public void notRanAuto_doInsert() {

    RCustomerKey customerKey = new RCustomerKey("compa", "coa");
    RCustomer rCustomer = new RCustomer();
    rCustomer.setKey(customerKey);
    rCustomer.setDescription("some foo for ms sql server");

    Ebean.save(rCustomer);

    ROrderPK k0 = new ROrderPK("compa", 100);
    ROrder rOrder = new ROrder();
    rOrder.setCustomer(rCustomer);
    rOrder.setOrderPK(k0);
    rOrder.setItem("Chair");

    Ebean.save(rOrder);
  }

  //@Test
  public void notRanAutomatically() {

    //EbeanServer server = CreateIdExpandedFormServer.create();
    EbeanServer server = Ebean.getServer(null);

    ROrderPK k0 = new ROrderPK("compa", 100);
    ROrderPK k1 = new ROrderPK("compa", 101);
    ROrderPK k2 = new ROrderPK("b", 105);
    ROrderPK k3 = new ROrderPK("c", 106);

    List<ROrderPK> keys = new ArrayList<>();
    keys.add(k0);
    keys.add(k1);
    keys.add(k2);
    keys.add(k3);

    Query<ROrder> query = server.find(ROrder.class).where().idIn(keys).query();

    query.findList();
    String sql = query.getGeneratedSql();

    Assert.assertTrue(sql.contains("(t0.company=? and t0.order_number=?) or"));

    Query<ROrder> query2 = server.find(ROrder.class).setId(k0);

    query2.findOne();
    sql = query2.getGeneratedSql();
    Assert.assertTrue(sql.contains("t0.company = ? "));
    Assert.assertTrue(sql.contains(" and t0.order_number = ?"));

    server.delete(ROrder.class, k0);

    server.delete(ROrder.class, keys);

  }

}
