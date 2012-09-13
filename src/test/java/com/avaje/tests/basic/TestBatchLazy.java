package com.avaje.tests.basic;

import com.avaje.ebean.AdminAutofetch;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.Query;
import com.avaje.tests.model.basic.Customer;
import com.avaje.tests.model.basic.Order;
import com.avaje.tests.model.basic.OrderDetail;
import com.avaje.tests.model.basic.ResetBasicData;
import junit.framework.TestCase;

import java.util.List;

public class TestBatchLazy extends TestCase {

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
