package com.avaje.tests.query;

import java.util.Set;

import junit.framework.Assert;
import junit.framework.TestCase;

import com.avaje.ebean.BeanState;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.Query;
import com.avaje.tests.model.basic.MyLobSize;

public class TestDefaultFetchLazy extends TestCase {

	public void testFetchTypeLazy(){
		
		MyLobSize m = new MyLobSize();
		m.setName("aname");
		m.setMyCount(10);
		m.setMyLob("A big lob of data");
		
		Ebean.save(m);

		Assert.assertNotNull(m.getId());

		MyLobSize myLobSize = Ebean.find(MyLobSize.class, m.getId());
		
		BeanState beanState = Ebean.getBeanState(myLobSize);
		Set<String> loadedProps = beanState.getLoadedProps();
		
		Assert.assertNotNull(loadedProps);
		Assert.assertTrue(loadedProps.contains("id"));
		Assert.assertTrue(loadedProps.contains("name"));
		
		// FetchType.LAZY properties excluded
		Assert.assertFalse(loadedProps.contains("myLob"));
		Assert.assertFalse(loadedProps.contains("myCount"));
		

		// the details is also tuned
		Query<MyLobSize> queryMany = Ebean.find(MyLobSize.class)
				.fetch("details")// ,"+query")
				.where().gt("id", 0).query();

		queryMany.findList();

		String generatedSql = queryMany.getGeneratedSql();
		Assert.assertTrue(generatedSql.contains("t1.other "));
		Assert.assertFalse(generatedSql.contains("t1.something "));
	}
	
}
