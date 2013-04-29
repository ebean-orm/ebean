package com.avaje.tests.basic;

import java.sql.Date;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.tests.model.basic.Order;
import com.avaje.tests.model.basic.OrderDetail;
import com.avaje.tests.model.basic.ResetBasicData;

public class TestManyLazyLoad extends BaseTestCase {

	@Test
	public void testLazyLoadRef() {
	
		ResetBasicData.reset();
		
		List<Order> list = Ebean.find(Order.class).order().asc("id").findList();
		Assert.assertTrue(list.size()+" > 0", list.size() > 0);
		
		// just use the first one
		Order order = list.get(0);
		
		// get it as a reference
		Order order1 = Ebean.getReference(Order.class, order.getId());
		Assert.assertNotNull(order1);
		
		Date orderDate = order1.getOrderDate();
		Assert.assertNotNull(orderDate);
		
		List<OrderDetail> details = order1.getDetails();
		
		// lazy load the details
		int sz = details.size();
		Assert.assertTrue(sz+" > 0", sz > 0);
		
		Order o = details.get(0).getOrder();
		Assert.assertTrue("same instance", o == order1);
		

		// change order... list before a scalar property
		Order order2 = Ebean.getReference(Order.class, order.getId());
		Assert.assertNotNull(order2);
		
		List<OrderDetail> details2 = order2.getDetails();
		
		// lazy load the details
		int sz2 = details2.size();
		Assert.assertTrue(sz2+" > 0", sz2 > 0);
		
		Order o2 = details2.get(0).getOrder();
		Assert.assertTrue("same instance", o2 == order2);

		
	}
	
}
