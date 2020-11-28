package org.tests.basic;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.Query;
import org.tests.model.basic.Customer;
import org.tests.model.basic.Order;
import org.tests.model.basic.OrderDetail;
import org.tests.model.basic.ResetBasicData;
import org.junit.Test;

import java.util.List;

public class TestBatchLazy extends BaseTestCase {

  @Test
  public void testMe() {

    ResetBasicData.reset();

    Query<Order> query = Ebean.find(Order.class);
    List<Order> list = query.findList();


    for (Order order : list) {
      Customer customer = order.getCustomer();
      customer.getName();

      List<OrderDetail> details = order.getDetails();
      for (OrderDetail orderDetail : details) {
        orderDetail.getProduct().getSku();
      }
    }

    Ebean.getDefaultServer().getAutoTune().collectProfiling();
    Ebean.getDefaultServer().getAutoTune().reportProfiling();

  }


}
