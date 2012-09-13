package com.avaje.tests.cache;

import java.util.List;

import org.junit.Assert;

import junit.framework.TestCase;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.bean.BeanCollection;
import com.avaje.tests.model.basic.Customer;
import com.avaje.tests.model.basic.ResetBasicData;

public class TestQueryCache extends TestCase {

	@SuppressWarnings("unchecked")
	public void test(){
		
		ResetBasicData.reset();
		
		List<Customer> list = Ebean.find(Customer.class)
			.setUseQueryCache(true)
			.setReadOnly(true)
			.where().ilike("name", "Rob")
			.findList();
		
		BeanCollection<Customer> bc = (BeanCollection<Customer>)list;
		Assert.assertFalse(bc.isReadOnly());
		Assert.assertFalse(bc.isEmpty());
		Assert.assertTrue(list.size() > 0);
		Assert.assertTrue(Ebean.getBeanState(list.get(0)).isReadOnly());
		
		
		List<Customer> list2 = Ebean.find(Customer.class)
			.setUseQueryCache(true)
			.setReadOnly(true)
			.where().ilike("name", "Rob")
			.findList();

		List<Customer> list2B = Ebean.find(Customer.class)
			.setUseQueryCache(true)
			//.setReadOnly(true)
			.where().ilike("name", "Rob")
			.findList();

		
//		Assert.assertTrue("same instance",list != list2);
//		
//		// readOnly defaults to true for query cache
//		Assert.assertTrue("same instance",list != list2B);
//
//		List<Customer> list3 = Ebean.find(Customer.class)
//			.setUseQueryCache(true)
//			.setReadOnly(false)
//			.where().ilike("name", "Rob")
//			.findList();
//
//		Assert.assertTrue("diff instance",list != list3);
//		BeanCollection<Customer> bc3 = (BeanCollection<Customer>)list3;
//		Assert.assertFalse(bc3.isReadOnly());
//		Assert.assertFalse(bc3.isEmpty());
//		Assert.assertTrue(list3.size() > 0);
//		Assert.assertFalse(Ebean.getBeanState(list3.get(0)).isReadOnly());


	}
	
}
