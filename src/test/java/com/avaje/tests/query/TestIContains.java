package com.avaje.tests.query;

import junit.framework.Assert;
import junit.framework.TestCase;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Query;
import com.avaje.tests.model.basic.Customer;
import com.avaje.tests.model.basic.ResetBasicData;

public class TestIContains extends TestCase {

	public void testIContains() {
		
		ResetBasicData.reset();
		
		// case insensitive
		Query<Customer> q0 = Ebean.find(Customer.class)
			.where().icontains("name", "Rob")
			.query();
		
		
		q0.findList();
		String generatedSql = q0.getGeneratedSql();
		
		Assert.assertTrue(generatedSql.contains("lower(t0.name)"));
		

		// not case insensitive
		q0 = Ebean.find(Customer.class)
		.where().contains("name", "Rob")
		.query();
	
	
		q0.findList();
		generatedSql = q0.getGeneratedSql();
		
		Assert.assertTrue(generatedSql.contains(" t0.name "));

		
	}
	
}
