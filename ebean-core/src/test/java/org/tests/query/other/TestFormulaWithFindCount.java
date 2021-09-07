package org.tests.query.other;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.EbeanServer;
import io.ebean.ExpressionList;
import org.tests.model.basic.Order;
import org.tests.model.basic.ResetBasicData;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TestFormulaWithFindCount extends BaseTestCase {

  @Test
  public void testFindCount() {

    ResetBasicData.reset();

    EbeanServer server = Ebean.getServer(null);


    ExpressionList<Order> ex = server.find(Order.class).select("id, status ,totalAmount").where().gt("totalAmount", 1d);
    List<Order> list = ex.findList();

    for (Order order : list) {
      Double amount = order.getTotalAmount();
      assertNotNull(amount);
    }

    ExpressionList<Order> expressionList = server.find(Order.class).where().gt("totalAmount", 1d);
    int rowCount = expressionList.findCount();
    assertEquals(list.size(), rowCount);
  }

}
