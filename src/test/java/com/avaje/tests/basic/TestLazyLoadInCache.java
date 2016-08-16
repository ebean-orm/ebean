package com.avaje.tests.basic;

import java.util.Map;
import java.util.Set;

import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.BeanState;
import com.avaje.ebean.Ebean;
import com.avaje.tests.model.basic.Address;
import com.avaje.tests.model.basic.Customer;
import com.avaje.tests.model.basic.ResetBasicData;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestLazyLoadInCache extends BaseTestCase {

	@Test
	public void testLoadInCache() {
		
		ResetBasicData.reset();
		
		Map<Integer, Customer> map = Ebean.find(Customer.class)
			.select("id, name")
			.setLoadBeanCache(true)
			.setReadOnly(true)
			.orderBy().asc("id")
			.findMap();
		
		assertTrue(!map.isEmpty());
		
		Object id = map.keySet().iterator().next();
		
		Customer cust1 = map.get(id);
		
		Customer cust1B = Ebean.find(Customer.class)
			.setReadOnly(true)
			.setId(id)
			.findUnique();
		
		assertTrue(cust1 != cust1B);
		
		Set<String> loadedProps = Ebean.getBeanState(cust1).getLoadedProps();
		
		assertTrue(loadedProps.contains("name"));
		assertFalse(loadedProps.contains("status"));
		
		cust1.getStatus();
		
		// a readOnly reference
		Address billingAddress = cust1.getBillingAddress();
		BeanState billAddrState = Ebean.getBeanState(billingAddress);
		assertTrue(billAddrState.isReference());
		assertTrue(billAddrState.isReadOnly());
		
		// lazy load .. no longer a reference
		billingAddress.getCity();
		assertFalse(billAddrState.isReference());
		
	}

}
