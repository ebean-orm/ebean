package com.avaje.tests.query;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Expr;
import com.avaje.tests.model.basic.OrderDetail;
import com.avaje.tests.model.basic.ResetBasicData;

import junit.framework.TestCase;

public class TestWhereRawClause extends TestCase {

  public void testRawClause() {
    
    ResetBasicData.reset();
    
    
    Ebean.find(OrderDetail.class)
      .where()
        .not(Expr.eq("id", 1))
        .raw("orderQty < shipQty")
        .findList();
    
  }
}
