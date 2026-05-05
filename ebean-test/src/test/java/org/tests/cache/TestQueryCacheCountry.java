package org.tests.cache;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import io.ebean.cache.ServerCache;
import io.ebean.cache.ServerCacheManager;
import io.ebean.cache.ServerCacheStatistics;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Country;
import org.tests.model.basic.ResetBasicData;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class TestQueryCacheCountry extends BaseTestCase {

  private final ServerCacheManager cacheManager = DB.cacheManager();
  private final ServerCache queryCache = cacheManager.queryCache(Country.class);
  private final ServerCache beanCache = cacheManager.beanCache(Country.class);

  private void clearCache() {
    queryCache.clear();
    beanCache.clear();
    queryCache.statistics(true);
  }

  @Test
  public void emptyQueryResult_expect_cached() {

    ResetBasicData.reset();
    clearCache();

    List<Country> countryList0 = DB.find(Country.class)
      .setUseQueryCache(true)
      .where().startsWith("name", "XLKMG")
      .orderBy().asc("name")
      .findList();

    assertThat(countryList0).isEmpty();

    ServerCacheStatistics queryStats = queryCache.statistics(false);
    assertEquals(1, queryStats.getMissCount());
    assertEquals(1, queryStats.getSize());

    List<Country> countryList1 = DB.find(Country.class)
      .setUseQueryCache(true)
      .where().startsWith("name", "XLKMG")
      .orderBy().asc("name")
      .findList();

    assertThat(countryList1).isEmpty();

    ServerCacheStatistics queryStats1 = queryCache.statistics(false);
    assertEquals(1, queryStats1.getMissCount());
    assertEquals(1, queryStats1.getSize());
    assertEquals(1, queryStats1.getHitCount());
  }

  @Test
  public void rawExpression() {

    ResetBasicData.reset();
    awaitL2Cache();
    clearCache();

    List<Country> countryList0 = DB.find(Country.class)
      .setUseQueryCache(true)
      .where().raw("code = ?", "NZ")
      .findList();

    assertThat(countryList0.get(0).getName()).isEqualTo("New Zealand");

    List<Country> countryList1 = DB.find(Country.class)
      .setUseQueryCache(true)
      .where().raw("code = ?", "AU")
      .findList();

    assertThat(countryList1.get(0).getName()).isEqualTo("Australia");

    ServerCacheStatistics queryStats1 = queryCache.statistics(false);
    assertEquals(2, queryStats1.getMissCount());
    assertEquals(2, queryStats1.getSize());
    assertEquals(0, queryStats1.getHitCount()); // no hits yet


    // we get a hit this time
    List<Country> countryList2 = DB.find(Country.class)
      .setUseQueryCache(true)
      .where().raw("code = ?", "NZ")
      .findList();

    assertThat(countryList2.get(0).getName()).isEqualTo("New Zealand");

    ServerCacheStatistics queryStats2 = queryCache.statistics(false);
    assertEquals(2, queryStats2.getMissCount());
    assertEquals(2, queryStats2.getSize());
    assertEquals(1, queryStats2.getHitCount()); // got a hit

    // we get a hit on AU
    List<Country> countryList3 = DB.find(Country.class)
      .setUseQueryCache(true)
      .where().raw("code = ?", "AU")
      .findList();

    assertThat(countryList3.get(0).getName()).isEqualTo("Australia");

    ServerCacheStatistics queryStats3 = queryCache.statistics(false);
    assertEquals(2, queryStats3.getMissCount());
    assertEquals(2, queryStats3.getSize());
    assertEquals(2, queryStats3.getHitCount()); // got another hit
  }

  @Test
  public void test() {

    ResetBasicData.reset();

    awaitL2Cache();
    clearCache();

    assertEquals(0, queryCache.statistics(false).getSize());

    List<Country> countryList0 = DB.find(Country.class)
      .setUseQueryCache(true)
      .orderBy().asc("name")
      .findList();

    assertEquals(1, queryCache.statistics(false).getSize());
    assertTrue(!countryList0.isEmpty());

    List<Country> countryList1 = DB.find(Country.class)
      .setUseQueryCache(true)
      .orderBy().asc("name")
      .findList();

    ServerCacheStatistics statistics = queryCache.statistics(false);
    assertEquals(1, statistics.getSize());
    assertEquals(1, statistics.getHitCount());
    assertSame(countryList1, countryList0);

    Country nz = DB.find(Country.class, "NZ");
    nz.setName("New Zealandia");
    DB.save(nz);
    awaitL2Cache();

    statistics = queryCache.statistics(false);
    assertEquals(0, statistics.getSize());

    List<Country> countryList2 = DB.find(Country.class)
      .setUseQueryCache(true)
      .orderBy().asc("name")
      .findList();

    assertNotSame(countryList2, countryList0);

    nz = DB.find(Country.class, "NZ");
    nz.setName("New Zealand");
    DB.save(nz);
  }

}
