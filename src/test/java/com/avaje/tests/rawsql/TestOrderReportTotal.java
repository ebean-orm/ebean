package com.avaje.tests.rawsql;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.Query;
import com.avaje.tests.model.basic.OrderAggregate;
import com.avaje.tests.model.basic.ResetBasicData;

public class TestOrderReportTotal extends BaseTestCase {

  @Test
  public void test() {

    ResetBasicData.reset();

    Query<OrderAggregate> query = Ebean.createQuery(OrderAggregate.class);

    List<OrderAggregate> list = query.findList();
    Assert.assertNotNull(list);

    Query<OrderAggregate> q2 = Ebean.createQuery(OrderAggregate.class);
    q2.where().gt("id", 1);
    q2.having().gt("totalItems", 1);

    List<OrderAggregate> l2 = q2.findList();
    Assert.assertNotNull(l2);

  }
}
