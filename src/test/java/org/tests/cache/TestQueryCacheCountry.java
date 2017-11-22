package org.tests.cache;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.cache.ServerCache;
import io.ebean.cache.ServerCacheManager;
import io.ebean.cache.ServerCacheStatistics;
import org.junit.Assert;
import org.junit.Test;
import org.tests.model.basic.Country;
import org.tests.model.basic.ResetBasicData;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

public class TestQueryCacheCountry extends BaseTestCase {

  private ServerCacheManager cacheManager = Ebean.getServerCacheManager();
  private ServerCache queryCache = cacheManager.getQueryCache(Country.class);
  private ServerCache beanCache = cacheManager.getBeanCache(Country.class);

  private void clearCache() {
    queryCache.clear();
    beanCache.clear();
    queryCache.getStatistics(true);
  }


  @Test
  public void emptyQueryResult_expect_cached() {

    ResetBasicData.reset();
    clearCache();

    List<Country> countryList0 = Ebean.find(Country.class)
      .setUseQueryCache(true)
      .where().startsWith("name", "XLKMG")
      .order().asc("name")
      .findList();

    assertThat(countryList0).isEmpty();

    ServerCacheStatistics queryStats = queryCache.getStatistics(false);
    assertEquals(1, queryStats.getMissCount());
    assertEquals(1, queryStats.getSize());

    List<Country> countryList1 = Ebean.find(Country.class)
      .setUseQueryCache(true)
      .where().startsWith("name", "XLKMG")
      .order().asc("name")
      .findList();

    assertThat(countryList1).isEmpty();

    ServerCacheStatistics queryStats1 = queryCache.getStatistics(false);
    assertEquals(1, queryStats1.getMissCount());
    assertEquals(1, queryStats1.getSize());
    assertEquals(1, queryStats1.getHitCount());
  }

  @Test
  public void test() {

    ResetBasicData.reset();

    awaitL2Cache();
    clearCache();

    assertEquals(0, queryCache.getStatistics(false).getSize());

    List<Country> countryList0 = Ebean.find(Country.class)
      .setUseQueryCache(true)
      .order().asc("name")
      .findList();

    assertEquals(1, queryCache.getStatistics(false).getSize());
    assertTrue(!countryList0.isEmpty());

    List<Country> countryList1 = Ebean.find(Country.class)
      .setUseQueryCache(true)
      .order().asc("name")
      .findList();

    ServerCacheStatistics statistics = queryCache.getStatistics(false);
    assertEquals(1, statistics.getSize());
    assertEquals(1, statistics.getHitCount());
    Assert.assertSame(countryList1, countryList0);

    Country nz = Ebean.find(Country.class, "NZ");
    nz.setName("New Zealandia");
    Ebean.save(nz);
    awaitL2Cache();

    statistics = queryCache.getStatistics(false);
    assertEquals(0, statistics.getSize());

    List<Country> countryList2 = Ebean.find(Country.class)
      .setUseQueryCache(true)
      .order().asc("name")
      .findList();

    assertNotSame(countryList2, countryList0);
  }

}
