package com.avaje.tests.basic;

import junit.framework.Assert;
import junit.framework.TestCase;

import com.avaje.ebean.Ebean;
import com.avaje.tests.model.basic.Customer;
import com.avaje.tests.model.basic.Order;
import com.avaje.tests.model.basic.ResetBasicData;

public class TestPersistenceContext extends TestCase {

	public void test() {
		
		ResetBasicData.reset();

		// implicit transaction with its own
		// persistence context
		Order oBefore = Ebean.find(Order.class, 1);

		Order order = null;
		
		// start a persistence context
		Ebean.beginTransaction();
		try {
			
			order = Ebean.find(Order.class, 1);
		
			// not the same instance ...as a different
			// persistence context
			Assert.assertTrue(order != oBefore);
		
			
			// finds an existing bean in the persistence context
			// ... so doesn't even execute a query
			Order o2 = Ebean.find(Order.class, 1);
			Order o3 = Ebean.getReference(Order.class, 1);
			
			// all the same instance
			Assert.assertTrue(order == o2);
			Assert.assertTrue(order == o3);
			
		} finally {
			Ebean.endTransaction();
		}

		// implicit transaction with its own 
		// persistence context
		Order oAfter = Ebean.find(Order.class, 1);

		Assert.assertTrue(oAfter != oBefore);
		Assert.assertTrue(oAfter != order);
		
		
		Order testOrder = ResetBasicData.createOrderCustAndOrder("testPC");
		Integer id = testOrder.getCustomer().getId();
		Integer orderId = testOrder.getId();

		// start a persistence context
		Ebean.beginTransaction();
		try {
			Customer customer = Ebean.find(Customer.class)
				.setUseCache(false)
				.setId(id)
				.findUnique();
		
			System.gc();
			Order order2 = Ebean.find(Order.class, orderId);
			Customer customer2 = order2.getCustomer();

	        Assert.assertEquals(customer.getId(),customer2.getId());

			Assert.assertTrue(customer == customer2);
			
		} finally {
			Ebean.endTransaction();
		}
	}
	
}
