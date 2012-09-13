package com.avaje.tests.query;

import java.util.List;

import junit.framework.Assert;
import junit.framework.TestCase;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Query;
import com.avaje.tests.model.basic.Customer;
import com.avaje.tests.model.basic.OrderShipment;
import com.avaje.tests.model.basic.ResetBasicData;

public class TestJoinQuery extends TestCase {
	
	public void test() {
		
		ResetBasicData.reset();
		
		// test that join to order.details is not included
		Query<Customer> query = Ebean.find(Customer.class)
			.setAutofetch(false)
			.fetch("orders")
			.fetch("orders.details");
		
		List<Customer> list = query.findList();
		Assert.assertTrue("has rows", list.size() > 0);
				
		// test that join to order.details is not included
		Query<OrderShipment> shipQuery = Ebean.find(OrderShipment.class)
			.setAutofetch(false)
			.fetch("order")
			.fetch("order.details");
		
		
		List<OrderShipment> shipList = shipQuery.findList();
		Assert.assertTrue("has rows", shipList.size() > 0);
	}
	
}
