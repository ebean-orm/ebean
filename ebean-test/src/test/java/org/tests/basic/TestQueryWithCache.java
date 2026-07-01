package org.tests.basic;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import io.ebean.Query;
import io.ebean.cache.ServerCache;
import io.ebean.cache.ServerCacheManager;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.server.core.CacheOptions;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Country;
import org.tests.model.basic.ResetBasicData;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class TestQueryWithCache extends BaseTestCase {

  @Test
  void testCountryDeploy() {

    ResetBasicData.reset();

    SpiEbeanServer server = (SpiEbeanServer) DB.getDefault();
    BeanDescriptor<Country> beanDescriptor = server.descriptor(Country.class);
    CacheOptions cacheOptions = beanDescriptor.cacheOptions();

    assertNotNull(cacheOptions);
    assertTrue(cacheOptions.isReadOnly());
    assertTrue(beanDescriptor.isCacheSharableBeans());

    ServerCacheManager serverCacheManager = server.cacheManager();
    serverCacheManager.clear(Country.class);

    ServerCache beanCache = serverCacheManager.beanCache(Country.class);
    assertEquals(0, beanCache.size());

    Country nz1 = DB.reference(Country.class, "NZ");
    assertEquals(0, beanCache.size());

    // has the effect of loading the cache via lazy loading
    nz1.getName();
    assertEquals(1, beanCache.size());

    Country nz2 = DB.reference(Country.class, "NZ");
    Country nz2b = DB.reference(Country.class, "NZ");

    Country nz3 = DB.find(Country.class, "NZ");

    Country nz4 = DB.find(Country.class).setId("NZ").setAutoTune(false).setUseCache(false)
      .findOne();

    assertThat(nz2).isNotSameAs(nz2b); // Changed behaviour with unmodifiable
    assertThat(nz2).isNotSameAs(nz3); // Changed behaviour with unmodifiable
    assertThat(nz3).isNotSameAs(nz4);
  }

  @Test
  void testSkipCache() {
    ResetBasicData.reset();

    DB.find(Country.class, "NZ");

    Query<Country> query = DB.find(Country.class).setId("NZ").setUseCache(false);
    query.findOne();

    assertSql(query).isNotNull();
  }

}
