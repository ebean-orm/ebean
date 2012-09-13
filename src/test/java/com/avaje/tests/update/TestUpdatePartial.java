package com.avaje.tests.update;

import junit.framework.TestCase;

import org.junit.Assert;

import com.avaje.ebean.Ebean;
import com.avaje.tests.model.basic.Customer;

public class TestUpdatePartial extends TestCase {

	
	public void test() {
		
		Customer c = new Customer();
		c.setName("TestUpdateMe");
		c.setStatus(Customer.Status.ACTIVE);
		c.setSmallnote("a note");

		Ebean.save(c);
		
		
		Customer c2 = Ebean.find(Customer.class, c.getId());
		Assert.assertNull("not partial",Ebean.getBeanState(c2).getLoadedProps());
		
		c2.setStatus(Customer.Status.INACTIVE);
		c2.setSmallnote("2nd note");
		
		Ebean.save(c2);

		Customer c3 = Ebean.find(Customer.class, c.getId());
		Assert.assertNull("not partial",Ebean.getBeanState(c3).getLoadedProps());
		
		c2.setStatus(Customer.Status.NEW);
		c2.setSmallnote("3rd note");
		
		Ebean.save(c2);

	}
}
