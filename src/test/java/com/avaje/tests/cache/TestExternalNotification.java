package com.avaje.tests.cache;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.SqlUpdate;
import com.avaje.ebean.cache.ServerCache;
import com.avaje.ebean.cache.ServerCacheManager;
import com.avaje.tests.model.basic.Country;
import com.avaje.tests.model.basic.ResetBasicData;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestExternalNotification extends BaseTestCase {

  @Test
  public void testNotify() {

    ResetBasicData.reset();

    loadCountryCache();

    ServerCache countryCache = Ebean.getServerCacheManager().getBeanCache(Country.class);

    assertTrue(countryCache.size() > 0);

    // inserts don't remove from bean cache
    Ebean.externalModification("o_country", true, false, false);
    assertTrue(countryCache.size() > 0);

    // updates flush cache
    Ebean.externalModification("o_country", false, true, false);
    assertEquals(0, countryCache.size());

    loadCountryCache();
    assertTrue(countryCache.size() > 0);

    // deletes flush cache
    Ebean.externalModification("o_country", false, false, true);
    assertEquals(0, countryCache.size());

    loadCountryCache();
    assertTrue(countryCache.size() > 0);

    ServerCacheManager serverCacheManager = Ebean.getServerCacheManager();
    serverCacheManager.clearAll();
    assertEquals(0, countryCache.size());

    loadCountryCache();
    assertTrue(countryCache.size() > 0);

    Ebean.getServerCacheManager().clear(Country.class);
    assertEquals(0, countryCache.size());

    loadCountryCache();
    int cacheSize = countryCache.size();
    assertTrue("cacheSize: " + cacheSize, cacheSize > 0);

    SqlUpdate sqlUpdate = Ebean
      .createSqlUpdate("update o_country set name = :name where code = :code");
    sqlUpdate.setParameter("name", "Aotearoa");
    sqlUpdate.setParameter("code", "NZ");

    // this should just clear the entire country cache
    int rows = sqlUpdate.execute();
    assertEquals(1, rows);
    assertEquals(0, countryCache.size());

    // set it back...
    sqlUpdate.setParameter("name", "New Zealand");
    sqlUpdate.setParameter("code", "NZ");
    rows = sqlUpdate.execute();
    assertEquals(1, rows);
  }
}
