package org.tests.cache;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.SqlUpdate;
import io.ebean.cache.ServerCache;
import io.ebean.cache.ServerCacheManager;
import org.tests.model.basic.Country;
import org.tests.model.basic.ResetBasicData;
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
