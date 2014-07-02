package com.avaje.tests.basic.event;

import org.junit.Assert;
import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.Query;
import com.avaje.tests.model.basic.ResetBasicData;
import com.avaje.tests.model.basic.TOne;

public class TestQueryAdapter extends BaseTestCase {

  @Test
	public void testSimple() {
		
		ResetBasicData.reset();
		
		TOne o = new TOne();
		o.setName("something");
		
		Ebean.save(o);
		
		//Ebean.find(TOne.class, o.getId());
		
		Query<TOne> queryFindId = Ebean.find(TOne.class)
			.setId(o.getId());
		
		TOne one = queryFindId.findUnique();
		Assert.assertNotNull(one);
		Assert.assertEquals(one.getId(), o.getId());
		String generatedSql = queryFindId.getGeneratedSql();
		Assert.assertTrue(generatedSql.contains(" 1=1"));
		
	}
}
