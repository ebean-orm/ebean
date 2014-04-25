package com.avaje.tests.basic;

import java.util.Map;
import java.util.Set;

import junit.framework.Assert;

import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.BeanState;
import com.avaje.ebean.Ebean;
import com.avaje.tests.model.basic.Address;
import com.avaje.tests.model.basic.Customer;
import com.avaje.tests.model.basic.ResetBasicData;

public class TestLazyLoadInCache extends BaseTestCase {

	@Test
	public void testLoadInCache() {
		
		ResetBasicData.reset();
		
		Map<?, Customer> map = Ebean.find(Customer.class)
			.select("id, name")
			.setLoadBeanCache(true)
			//.setUseCache(true)
			.setReadOnly(true)
			.orderBy().asc("id")
			.findMap();
		
		Assert.assertTrue(map.size() > 0);
		
		Object id = map.keySet().iterator().next();
		
		Customer cust1 = map.get(id);
		
		Customer cust1B = Ebean.find(Customer.class)
			.setReadOnly(true)
			.setUseCache(true)
			.setId(id)
			.findUnique();
		
		Assert.assertTrue(cust1 != cust1B);
		
		Set<String> loadedProps = Ebean.getBeanState(cust1).getLoadedProps();
		
		Assert.assertTrue(loadedProps.contains("name"));
		Assert.assertFalse(loadedProps.contains("status"));
		
		cust1.getStatus();
		
		// a readOnly reference
		Address billingAddress = cust1.getBillingAddress();
		BeanState billAddrState = Ebean.getBeanState(billingAddress);
		Assert.assertTrue(billAddrState.isReference());
		Assert.assertTrue(billAddrState.isReadOnly());
		
		// lazy load .. no longer a reference
		billingAddress.getCity();
		Assert.assertFalse(billAddrState.isReference());
		
	}

}
