package com.avaje.tests.basic;

import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.Query;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.tests.model.basic.Order;
import com.avaje.tests.model.basic.ResetBasicData;


public class TestLimitQuery extends BaseTestCase {

  @Test
	public void testNothing() {
		
	}

  @Test
	public void testLimitWithMany() {
		rob();
		rob();
	}
  
	private void rob() {
		ResetBasicData.reset();
		
		SpiEbeanServer server = (SpiEbeanServer)Ebean.getServer(null);
		boolean h2Db = "h2".equals(server.getDatabasePlatform().getName());
		
		Query<Order> query = Ebean.find(Order.class)
			.setAutofetch(false)
			.fetch("details")
			.where().gt("details.id", 0)
			.setMaxRows(10);
			//.findList();
		
		List<Order> list = query.findList();
		
		Assert.assertTrue("sz > 0", list.size() > 0);
	
		String sql = query.getGeneratedSql();
		boolean hasDetailsJoin = sql.indexOf("join o_order_detail") > -1;
		boolean hasLimit = sql.indexOf("limit 11") > -1;
		boolean hasSelectedDetails = sql.indexOf("od.id,") > -1;
		boolean hasDistinct = sql.indexOf("select distinct") > -1;
		
		Assert.assertTrue(hasDetailsJoin);
		Assert.assertFalse(hasSelectedDetails);
		Assert.assertTrue(hasDistinct);
		if (h2Db){
			Assert.assertTrue(hasLimit);
		}
		
		query = Ebean.find(Order.class)
			.setAutofetch(false)
			.fetch("details")
			.setMaxRows(10);
		
		query.findList();
		
		sql = query.getGeneratedSql();
		hasDetailsJoin = sql.indexOf("left outer join o_order_detail") > -1;
		hasLimit = sql.indexOf("limit 11") > -1;
		hasSelectedDetails = sql.indexOf("od.id") > -1;
		hasDistinct = sql.indexOf("select distinct") > -1;
	
		Assert.assertFalse("no join with maxRows",hasDetailsJoin);
		Assert.assertFalse(hasSelectedDetails);
		Assert.assertFalse(hasDistinct);
		if (h2Db){
			Assert.assertTrue(hasLimit);			
		}
	}
}
