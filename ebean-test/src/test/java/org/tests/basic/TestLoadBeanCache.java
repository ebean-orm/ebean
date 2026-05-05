package org.tests.basic;

import io.ebean.CacheMode;
import io.ebean.Query;
import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import io.ebean.test.LoggedSql;
import io.ebeaninternal.api.SpiQuery;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Country;
import org.tests.model.basic.Customer;
import org.tests.model.basic.ResetBasicData;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertSame;

class TestLoadBeanCache extends BaseTestCase {

  @Test
  void loadBeanCache_false() {
    Query<Country> query = DB.find(Country.class).setBeanCacheMode(CacheMode.OFF);

    SpiQuery<?> spiQuery = (SpiQuery<?>) query;
    assertThat(spiQuery.isBeanCachePut()).isFalse();
  }

  @Test
  void testLoad() {

    ResetBasicData.reset();

    Map<String, Country> map = DB.find(Country.class)
      .setBeanCacheMode(CacheMode.PUT)
      .setUseQueryCache(true)
      .setUnmodifiable(true)
      .orderBy("name")
      .findMap();

    Country loadedNz = map.get("NZ");

    // this will hit the cache, with setUnmodifiable(true) we can use shared bean instances
    Country nz = DB.find(Country.class).setId("NZ").setUnmodifiable(true).findOne();

    assertSame(loadedNz, nz);
  }

  @Test
  void testLoadWithFindMap() {

    ResetBasicData.reset();

    List<Object> ids = DB.find(Customer.class).findIds();
    assertThat(ids).isNotEmpty();

    DB.getDefault().pluginApi().cacheManager().clearAll();

    // hit database
    LoggedSql.start();
    DB.find(Customer.class).where().idIn(ids).findMap();
    List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(1);

    // hit beanCache
    LoggedSql.start();
    DB.find(Customer.class).where().idIn(ids).findMap();
    sql = LoggedSql.stop();
    assertThat(sql).hasSize(0);
  }
}
