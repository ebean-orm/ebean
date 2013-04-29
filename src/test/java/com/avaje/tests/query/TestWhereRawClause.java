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

    Ebean.find(OrderDetail.class).where().not(Expr.eq("id", 1)).raw("orderQty < shipQty")
        .findList();

  }
}
