package com.avaje.tests.basic;

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import junit.framework.Assert;

import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.bean.BeanCollection;
import com.avaje.tests.model.basic.Address;
import com.avaje.tests.model.basic.Customer;
import com.avaje.tests.model.basic.Order;
import com.avaje.tests.model.basic.OrderDetail;
import com.avaje.tests.model.basic.ResetBasicData;

public class TestReadOnlyPropagation extends BaseTestCase {

  @Test
	public void testReadOnly() {
		
		ResetBasicData.reset();
		
		Order order = Ebean.find(Order.class)
			.setAutofetch(false)
			.setUseCache(false)
			.setReadOnly(true)
			.setId(1)
			.findUnique();
		
		Assert.assertTrue(Ebean.getBeanState(order).isReadOnly());

		
		Customer customer = order.getCustomer();
		Assert.assertTrue(Ebean.getBeanState(customer).isReadOnly());
		
		Address billingAddress = customer.getBillingAddress();
		Assert.assertTrue(Ebean.getBeanState(billingAddress).isReadOnly());
		
		
		List<OrderDetail> details = order.getDetails();
		BeanCollection<?> bc = (BeanCollection<?>)details;
	
		Assert.assertTrue(bc.isReadOnly());
		Assert.assertTrue(!bc.isPopulated());
		
		bc.size();
		Assert.assertTrue(bc.size() > 0);
		Assert.assertTrue(bc.isReadOnly());
		Assert.assertTrue(bc.isPopulated());
		try {
			details.add(new OrderDetail());
			Assert.assertTrue(false);			
		} catch (IllegalStateException e){
			Assert.assertTrue(true);			
		}
		try {
			details.remove(0);
			Assert.assertTrue(false);			
		} catch (IllegalStateException e){
			Assert.assertTrue(true);			
		}
		try {
			Iterator<OrderDetail> it = details.iterator();
			it.next();
			it.remove();
			Assert.assertTrue(false);			
		} catch (IllegalStateException e){
			Assert.assertTrue(true);			
		}
		try {
			ListIterator<OrderDetail> it = details.listIterator();
			it.next();
			it.remove();
			Assert.assertTrue(false);			
		} catch (IllegalStateException e){
			Assert.assertTrue(true);			
		}
		try {
			List<OrderDetail> subList = details.subList(0, 1);
			subList.remove(0);
			Assert.assertTrue(false);			
		} catch (UnsupportedOperationException e){
			Assert.assertTrue(true);			
		}

		
	}
	
}
