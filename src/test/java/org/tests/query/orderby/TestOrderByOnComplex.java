package org.tests.query.orderby;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.Query;
import org.tests.model.basic.Order;
import org.tests.model.basic.ResetBasicData;
import org.junit.Assert;
import org.junit.Test;

public class TestOrderByOnComplex extends BaseTestCase {

  @Test
  public void test() {

    ResetBasicData.reset();

    Query<Order> query = Ebean.find(Order.class).order().desc("customer");

    query.findList();

    String sql = query.getGeneratedSql();
    Assert.assertTrue(sql.contains("order by t0.kcustomer_id desc"));

  }

}
