package com.avaje.tests.basic;

import java.util.List;

import junit.framework.TestCase;

import org.junit.Assert;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Query;
import com.avaje.tests.model.basic.TMapSuperEntity;

public class TestMappedSuper extends TestCase {

	public void test() {
		
		TMapSuperEntity e = new TMapSuperEntity();
		e.setName("babana");
		
		Ebean.save(e);
		
		// select includes a transient property
		TMapSuperEntity e2 = Ebean.find(TMapSuperEntity.class)
			.where().idEq(e.getId())
			.select("id, name, myint, someObject, bananan")
			.findUnique();
		
		Assert.assertNotNull(e2);
		
		// using a raw SQL query that populates a transient field
		Query<TMapSuperEntity> query = Ebean.createNamedQuery(TMapSuperEntity.class, "testTransient");
		
		List<TMapSuperEntity> list = query.where()
			.gt("id", 0)
			.istartsWith("name", "bab")
			.findList();
		
		Assert.assertTrue(list.size() >= 1);
		TMapSuperEntity e3 = list.get(0);
		Integer myint = e3.getMyint();
		Assert.assertEquals(Integer.valueOf(12), myint);
		
		TMapSuperEntity eSaveDelete = new TMapSuperEntity();
		eSaveDelete.setName("babana");
		
		Ebean.save(eSaveDelete);
		
		Ebean.delete(eSaveDelete);
	}
	
}
