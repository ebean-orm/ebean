package org.tests.cache;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import io.ebean.SqlUpdate;
import io.ebean.cache.ServerCache;
import io.ebean.cache.ServerCacheManager;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Country;
import org.tests.model.basic.ResetBasicData;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestExternalNotification extends BaseTestCase {

  @Test
  public void testNotify() {

    ResetBasicData.reset();

    loadCountryCache();

    ServerCache countryCache = DB.cacheManager().beanCache(Country.class);

    assertTrue(countryCache.size() > 0);

    // inserts don't remove from bean cache
    DB.externalModification("o_country", true, false, false);
    assertTrue(countryCache.size() > 0);

    // updates flush cache
    DB.externalModification("o_country", false, true, false);
    assertEquals(0, countryCache.size());

    loadCountryCache();
    assertTrue(countryCache.size() > 0);

    // deletes flush cache
    DB.externalModification("o_country", false, false, true);
    assertEquals(0, countryCache.size());

    loadCountryCache();
    assertTrue(countryCache.size() > 0);

    ServerCacheManager serverCacheManager = DB.cacheManager();
    serverCacheManager.clearAll();
    assertEquals(0, countryCache.size());

    loadCountryCache();
    assertTrue(countryCache.size() > 0);

    DB.cacheManager().clear(Country.class);
    assertEquals(0, countryCache.size());

    loadCountryCache();
    int cacheSize = countryCache.size();
    assertTrue(cacheSize > 0);

    SqlUpdate sqlUpdate = DB.sqlUpdate("update o_country set name = :name where code = :code");
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
