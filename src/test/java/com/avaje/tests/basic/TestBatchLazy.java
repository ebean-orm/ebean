package com.avaje.tests.basic;

import java.util.List;

import org.junit.Test;

import com.avaje.ebean.AdminAutofetch;
import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.Query;
import com.avaje.tests.model.basic.Customer;
import com.avaje.tests.model.basic.Order;
import com.avaje.tests.model.basic.OrderDetail;
import com.avaje.tests.model.basic.ResetBasicData;

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
		
		AdminAutofetch adminAutofetch = Ebean.getServer(null).getAdminAutofetch();
		adminAutofetch.collectUsageViaGC();

	}
	
	
	
}
