package com.avaje.tests.basic;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.cache.ServerCache;
import com.avaje.ebean.cache.ServerCacheManager;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.ebeaninternal.server.core.CacheOptions;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.tests.model.basic.Country;
import com.avaje.tests.model.basic.ResetBasicData;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class TestQueryWithCache extends BaseTestCase {

  @Test
  public void testCountryDeploy() {

    ResetBasicData.reset();

    SpiEbeanServer server = (SpiEbeanServer) Ebean.getServer(null);
    BeanDescriptor<Country> beanDescriptor = server.getBeanDescriptor(Country.class);
    CacheOptions cacheOptions = beanDescriptor.getCacheOptions();

    assertNotNull(cacheOptions);
    assertTrue(cacheOptions.isReadOnly());
    assertTrue(beanDescriptor.isCacheSharableBeans());

    ServerCacheManager serverCacheManager = server.getServerCacheManager();
    serverCacheManager.clear(Country.class);

    ServerCache beanCache = serverCacheManager.getBeanCache(Country.class);
    assertEquals(0, beanCache.size());

    Country nz1 = Ebean.getReference(Country.class, "NZ");
    assertEquals(0, beanCache.size());

    // has the effect of loading the cache via lazy loading
    nz1.getName();
    assertEquals(1, beanCache.size());

    Country nz2 = Ebean.getReference(Country.class, "NZ");
    Country nz2b = Ebean.getReference(Country.class, "NZ");

    Country nz3 = Ebean.find(Country.class, "NZ");

    Country nz4 = Ebean.find(Country.class).setId("NZ").setAutoTune(false).setUseCache(false)
        .findUnique();

    assertTrue(nz2 == nz2b);
    assertTrue(nz2 == nz3);
    assertTrue(nz3 != nz4);

  }
}
