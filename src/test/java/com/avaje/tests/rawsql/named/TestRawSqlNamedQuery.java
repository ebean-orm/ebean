package com.avaje.tests.rawsql.named;

import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.FetchConfig;
import com.avaje.ebean.Query;
import com.avaje.tests.model.basic.OrderAggregate;
import com.avaje.tests.model.basic.ResetBasicData;

public class TestRawSqlNamedQuery extends BaseTestCase {

  @Test
  public void test() {

    ResetBasicData.reset();

    Query<OrderAggregate> q = Ebean.createNamedQuery(OrderAggregate.class, "total.amount");
    q.fetch("order", new FetchConfig().query());

    q.findList();
  }

}
