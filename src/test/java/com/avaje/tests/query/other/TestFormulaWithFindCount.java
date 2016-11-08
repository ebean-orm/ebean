package com.avaje.tests.query.other;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.ExpressionList;
import com.avaje.tests.model.basic.Order;
import com.avaje.tests.model.basic.ResetBasicData;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class TestFormulaWithFindCount extends BaseTestCase {

  @Test
  public void testFindCount() {

    ResetBasicData.reset();

    EbeanServer server = Ebean.getServer(null);


    ExpressionList<Order> ex = server.find(Order.class).select("id, status ,totalAmount").where().gt("totalAmount", 1d);
    List<Order> list = ex.findList();

    for (Order order : list) {
      Double amount = order.getTotalAmount();
      Assert.assertNotNull(amount);
    }

    ExpressionList<Order> expressionList = server.find(Order.class).where().gt("totalAmount", 1d);
    int rowCount = expressionList.findCount();
    Assert.assertEquals(list.size(), rowCount);
  }

}
