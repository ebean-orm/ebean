package org.tests.basic;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import org.tests.model.basic.Order;
import org.tests.model.basic.ResetBasicData;
import org.junit.Test;

public class TestBatchLazyMany extends BaseTestCase {

  @Test
  public void testMe() {

    ResetBasicData.reset();

    Order order2 = Ebean.getReference(Order.class, 1);
    order2.getOrderDate();

//		List<Order> list = Ebean.find(Order.class)
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
