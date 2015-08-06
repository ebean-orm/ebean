package com.avaje.tests.query;

import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.Query;
import com.avaje.tests.model.basic.Order;
import com.avaje.tests.model.basic.ResetBasicData;

public class TestQueryLimitOffsetSimple extends BaseTestCase {

  /**
   * Test the syntax of the limit offset clause.
   */
  @Test
  public void testMe() {

    ResetBasicData.reset();

    Query<Order> query = Ebean.createQuery(Order.class, "where status = :A limit 100 offset 3");
    query.setParameter("A", Order.Status.NEW);

    query
        .setFirstRow(10)
        .order().asc("id")
        .findList();

  }

}
