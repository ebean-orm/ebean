package org.integration;

import io.ebean.DB;
import io.ebean.Database;
import io.ebean.cache.ServerCache;
import io.ebean.cache.ServerCacheStatistics;
import org.domain.FRCust;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class RedissonTenantAwareCacheTest {

  private static final ThreadLocal<String> TENANT = new ThreadLocal<>();

  private Database buildTenantDb() {
    return Database.builder()
      .dataSource(DB.getDefault().pluginApi().dataSource())
      .loadFromProperties()
      .defaultDatabase(false)
      .name("tenant-test")
      .ddlGenerate(false)
      .ddlRun(false)
      .currentTenantProvider(TENANT::get)
      .build();
  }

  @Test
  void singleBean_tenantA_hits_tenantB_misses() {
    Database db = buildTenantDb();
    try {
      FRCust cust = new FRCust("t-single-iso");
      DB.save(cust);
      long id = cust.getId();

      ServerCache beanCache = db.cacheManager().beanCache(FRCust.class);
      beanCache.clear();

      // Tenant A: first load – goes to DB
      TENANT.set("tenantA");
      beanCache.statistics(true); // reset counters
      assertThat(db.find(FRCust.class, id)).isNotNull();
      assertThat(Objects.requireNonNull(beanCache.statistics(true)).getMissCount()).isEqualTo(1);

      // Tenant A: second load – must hit cache
      assertThat(db.find(FRCust.class, id)).isNotNull();
      assertThat(Objects.requireNonNull(beanCache.statistics(true)).getHitCount()).isEqualTo(1);

      // Tenant B: same ID, different tenant key – must miss
      TENANT.set("tenantB");
      beanCache.statistics(true); // reset counters
      assertThat(db.find(FRCust.class, id)).isNotNull();
      ServerCacheStatistics statsB = beanCache.statistics(true);
      assertNotNull(statsB);
      assertThat(statsB.getHitCount()).isEqualTo(0);
      assertThat(statsB.getMissCount()).isEqualTo(1);

    } finally {
      TENANT.remove();
      db.shutdown(false, false);
    }
  }

  @Test
  void getAll_tenantA_hits_tenantB_misses() throws InterruptedException {
    Database db = buildTenantDb();
    try {
      List<FRCust> custs = new ArrayList<>();
      for (String n : new String[]{"tga0", "tga1", "tga2"}) {
        custs.add(new FRCust(n));
      }
      DB.saveAll(custs);
      List<Long> ids = custs.stream().map(FRCust::getId).collect(Collectors.toList());

      ServerCache beanCache = db.cacheManager().beanCache(FRCust.class);
      beanCache.clear();

      // Tenant A: first batch load – DB misses, cache populated
      TENANT.set("tenantA");
      List<FRCust> listA0 = db.find(FRCust.class).where().idIn(ids).setUseCache(true).findList();
      assertThat(listA0).hasSize(3);

      Thread.sleep(10);

      // Tenant A: second batch load – all 3 must be cache hits
      beanCache.statistics(true); // reset
      List<FRCust> listA1 = db.find(FRCust.class).where().idIn(ids).setUseCache(true).findList();
      assertThat(listA1).hasSize(3);
      ServerCacheStatistics statsA = beanCache.statistics(true);
      assertNotNull(statsA);
      assertThat(statsA.getHitCount()).isEqualTo(3);
      assertThat(statsA.getMissCount()).isEqualTo(0);

      // Tenant B: same IDs, different tenant – all 3 must miss
      TENANT.set("tenantB");
      beanCache.statistics(true); // reset
      List<FRCust> listB = db.find(FRCust.class).where().idIn(ids).setUseCache(true).findList();
      assertThat(listB).hasSize(3);
      ServerCacheStatistics statsB = beanCache.statistics(true);
      assertNotNull(statsB);
      assertThat(statsB.getHitCount()).isEqualTo(0);
      assertThat(statsB.getMissCount()).isEqualTo(3);

    } finally {
      TENANT.remove();
      db.shutdown(false, false);
    }
  }
}
