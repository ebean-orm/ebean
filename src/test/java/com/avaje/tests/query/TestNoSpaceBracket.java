package com.avaje.tests.query;

import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.Query;
import com.avaje.tests.model.basic.Order;

public class TestNoSpaceBracket extends BaseTestCase {

  @Test
  public void test() {

    Query<Order> query = Ebean.createQuery(Order.class, "find order join customer(id,name) ");

    query.findList();

  }

}
