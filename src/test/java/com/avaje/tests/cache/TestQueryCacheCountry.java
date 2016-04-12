package com.avaje.tests.cache;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.cache.ServerCache;
import com.avaje.ebean.cache.ServerCacheStatistics;
import com.avaje.tests.model.basic.Country;
import com.avaje.tests.model.basic.ResetBasicData;

public class TestQueryCacheCountry extends BaseTestCase {
  
  @Test
  public void test() {
    
    ResetBasicData.reset();
    
    ServerCache queryCache = Ebean.getServerCacheManager().getQueryCache(Country.class);
    queryCache.clear();

    ServerCache beanCache = Ebean.getServerCacheManager().getBeanCache(Country.class);
    beanCache.clear();

    Assert.assertEquals(0, queryCache.getStatistics(false).getSize());
    
    List<Country> countryList0 = Ebean.find(Country.class)
      .setUseQueryCache(true)
      .order().asc("name")
      .findList();
    
    Assert.assertEquals(1, queryCache.getStatistics(false).getSize());
    Assert.assertTrue(countryList0.size() > 0);
    
    List<Country> countryList1 = Ebean.find(Country.class)
        .setUseQueryCache(true)
        .order().asc("name")
        .findList();
      
    ServerCacheStatistics statistics = queryCache.getStatistics(false);
    Assert.assertEquals(1, statistics.getSize());
    Assert.assertEquals(1, statistics.getHitCount());
    Assert.assertSame(countryList1, countryList0);
    
    Country nz = Ebean.find(Country.class, "NZ");
    nz.setName("New Zealandia");
    Ebean.save(nz);
    awaitL2Cache();

    statistics = queryCache.getStatistics(false);
    Assert.assertEquals(0, statistics.getSize());
    
    List<Country> countryList2 = Ebean.find(Country.class)
        .setUseQueryCache(true)
        .order().asc("name")
        .findList();
  
    Assert.assertNotSame(countryList2, countryList0);
  }

}
