package com.avaje.tests.batchload;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.tests.model.basic.Contact;
import com.avaje.tests.model.basic.Customer;

public class TestBasicNavOnEmpty extends BaseTestCase {

  @Test
	public void test() {
		
		Customer c = new Customer();
		c.setName("HelloRob");
		
		Ebean.save(c);
		
		c = Ebean.find(Customer.class, c.getId());
		
		List<Contact> contacts = c.getContacts();
		Assert.assertEquals(0,contacts.size());
	}
	
}
