package com.avaje.tests.cache;

import junit.framework.Assert;
import junit.framework.TestCase;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.SqlUpdate;
import com.avaje.ebean.cache.ServerCache;
import com.avaje.ebean.cache.ServerCacheManager;
import com.avaje.tests.model.basic.Country;
import com.avaje.tests.model.basic.ResetBasicData;

public class TestExternalNotification extends TestCase {

	
	public void testNotify() {
		
		ResetBasicData.reset();
		
		Ebean.runCacheWarming(Country.class);
		
		ServerCache countryCache = Ebean.getServerCacheManager().getBeanCache(Country.class);
		
		Assert.assertTrue(countryCache.size() > 0);
		
		// inserts don't remove from bean cache
		Ebean.externalModification("o_country", true, false, false);
		Assert.assertTrue(countryCache.size() > 0);

		// updates flush cache
		Ebean.externalModification("o_country", false, true, false);
		Assert.assertEquals(0, countryCache.size());

		Ebean.runCacheWarming(Country.class);
		Assert.assertTrue(countryCache.size() > 0);

		// deletes flush cache
		Ebean.externalModification("o_country", false, false, true);
		Assert.assertEquals(0, countryCache.size());

		Ebean.runCacheWarming(Country.class);
		Assert.assertTrue(countryCache.size() > 0);

		ServerCacheManager serverCacheManager = Ebean.getServerCacheManager();
		serverCacheManager.clearAll();
		Assert.assertEquals(0, countryCache.size());

		Ebean.runCacheWarming(Country.class);
		Assert.assertTrue(countryCache.size() > 0);

		Ebean.getServerCacheManager().clear(Country.class);
		Assert.assertEquals(0, countryCache.size());

		Ebean.runCacheWarming(Country.class);
		int cacheSize = countryCache.size();
		Assert.assertTrue("cacheSize: "+cacheSize,cacheSize > 0);

		SqlUpdate sqlUpdate = Ebean.createSqlUpdate("update o_country set name = :name where code = :code");
		sqlUpdate.setParameter("name", "Aotearoa");
		sqlUpdate.setParameter("code","NZ");
		
		// this should just clear the entire country cache
		int rows = sqlUpdate.execute();
		Assert.assertEquals(1, rows);
		Assert.assertEquals(0, countryCache.size());

		// set it back...
		sqlUpdate.setParameter("name","New Zealand");
		sqlUpdate.setParameter("code","NZ");
		rows = sqlUpdate.execute();
		Assert.assertEquals(1, rows);
	}
}
