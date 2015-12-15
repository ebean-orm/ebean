package com.avaje.tests.query;

import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.Expr;
import com.avaje.tests.model.basic.OrderDetail;
import com.avaje.tests.model.basic.ResetBasicData;

public class TestWhereRawClause extends BaseTestCase {

  @Test
  public void testRawClause() {

    ResetBasicData.reset();

    Ebean.find(OrderDetail.class)
        .where()
        .not(Expr.eq("id", 1))
        .raw("orderQty < shipQty")
        .findList();

  }

  @Test
  public void testRawWithBindParams() {

    ResetBasicData.reset();

    Ebean.find(OrderDetail.class)
        .where()
        .ne("id", 42)
        .raw("orderQty < ?", 100)
        .gt("id", 1)
        .raw("unitPrice > ? and product.id > ?", new Object[]{2,3})
        .findList();

  }
}
