package com.avaje.tests.basic;

import java.util.List;
import java.util.concurrent.ExecutionException;

import org.junit.Assert;
import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.FutureIds;
import com.avaje.ebean.Query;
import com.avaje.tests.model.basic.Order;
import com.avaje.tests.model.basic.ResetBasicData;

public class TestFetchId extends BaseTestCase {
  
  @Test
	public void testFetchId() throws InterruptedException, ExecutionException {
		
		ResetBasicData.reset();
		
		Query<Order> query = Ebean.find(Order.class)
			.setAutoTune(false)
			.fetch("details")
			.where().gt("id", 1)
			.gt("details.id", 0)
			.query();
		 
		List<Object> ids = Ebean.getServer(null).findIds(query, null);

		FutureIds<Order> futureIds = Ebean.getServer(null).findFutureIds(query,null);

		// this list is likely empty at this point and
		// will get populated in the background
		List<Object> partial = futureIds.getPartialIds();

		// this is likely 0 or a small number

		// wait for all the id's to be fetched
		List<Object> idList = futureIds.get();
		Assert.assertTrue("same instance", partial == idList);

		Assert.assertTrue("sz > 0", ids.size() > 0);
	}
}
