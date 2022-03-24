package org.tests.basic;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Order;
import org.tests.model.basic.ResetBasicData;

public class TestBatchLazyMany extends BaseTestCase {

  @Test
  public void testMe() {

    ResetBasicData.reset();

    Order order2 = DB.reference(Order.class, 1);
    order2.getOrderDate();

//		List<Order> list = DB.find(Order.class)
//			//.join("details")
//			//.join("details", "+fetchquery")
//			.findList();
//
//		Order order = list.get(0);
//		//List<OrderDetail> details = order.getDetails();
//		//details.size();
//
//		Customer customer = order.getCustomer();
//		customer.getName();
//		System.out.println("done");

  }


}
