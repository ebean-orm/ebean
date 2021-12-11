package org.tests.basic;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.test.LoggedSql;

import org.junit.jupiter.api.Test;
import org.tests.model.basic.Country;
import org.tests.model.basic.Customer;
import org.tests.model.basic.ResetBasicData;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class TestLoadBeanCache extends BaseTestCase {

  @Test
  void testLoad() {

    ResetBasicData.reset();

    Map<String, Country> map = DB.find(Country.class)
      .setLoadBeanCache(true)
      .setUseQueryCache(true)
      .setReadOnly(true)
      .order("name")
      .findMap();

    Country loadedNz = map.get("NZ");

    // this will hit the cache
    Country nz = DB.find(Country.class, "NZ");

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
