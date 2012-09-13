package com.avaje.tests.query;

import junit.framework.TestCase;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Query;
import com.avaje.tests.model.basic.Order;
import com.avaje.tests.model.basic.ResetBasicData;

public class TestQueryLimitOffsetSimple extends TestCase {

	/**
	 * Test the syntax of the limit offset clause.
	 */
	public void testMe() {
		
		ResetBasicData.reset();
		
		Query<Order> query = Ebean.createQuery(Order.class, "where status = :A limit 100 offset 3");
		query.setParameter("A",Order.Status.NEW);
		
		query
			.setFirstRow(10)
			//.setMaxRows(100)
			.findList();
		
	}
	
}
