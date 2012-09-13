package com.avaje.tests.basic;

import junit.framework.Assert;
import junit.framework.TestCase;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Query;
import com.avaje.tests.model.basic.Address;
import com.avaje.tests.model.basic.Customer;
import com.avaje.tests.model.basic.Order;
import com.avaje.tests.model.basic.ResetBasicData;

public class TestAssocOneEqExpression extends TestCase {

	public void test() {
		
		ResetBasicData.reset();
		
		Customer c = new Customer();
		c.setId(1);
		
		Query<Order> query = Ebean.find(Order.class)
			.where().eq("customer", c)
			.query();
		
		query.findList();
		String sql = query.getGeneratedSql();
		Assert.assertTrue(sql.contains("where t0.kcustomer_id = ?"));
		
		Address b = new Address();
		b.setId((short)1);
		
		Query<Order> q2 = Ebean.find(Order.class)
			.where().eq("customer.billingAddress", b)
			.query();
		
		q2.findList();
		sql = q2.getGeneratedSql();
		Assert.assertTrue(sql.contains("where t1.billing_address_id = ?"));

	}
}
