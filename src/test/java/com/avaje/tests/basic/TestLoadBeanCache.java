package com.avaje.tests.basic;

import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.tests.model.basic.Country;
import com.avaje.tests.model.basic.ResetBasicData;

public class TestLoadBeanCache extends BaseTestCase {

  @Test
	public void testLoad() {
		
		ResetBasicData.reset();
		
		Map<?, Country> map = Ebean.find(Country.class)
			.setLoadBeanCache(true)
			.setUseQueryCache(true)
			.setReadOnly(true)
			.order("name")
			.findMap();
		
		Country loadedNz = map.get("NZ");
		
		// this will hit the cache
		Country nz = Ebean.find(Country.class, "NZ");
		
		Assert.assertTrue(loadedNz == nz);
	}
}
