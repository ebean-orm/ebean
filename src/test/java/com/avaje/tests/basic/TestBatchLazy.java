package com.avaje.tests.basic;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.Query;
import com.avaje.tests.model.basic.Customer;
import com.avaje.tests.model.basic.Order;
import com.avaje.tests.model.basic.OrderDetail;
import com.avaje.tests.model.basic.ResetBasicData;
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
