package org.integration;

import io.ebean.DB;
import io.ebean.Database;
import io.ebean.redisson.DuelCache;
import org.domain.FPerson;
import org.domain.query.QFPerson;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ClusterTest {

  private static Database db;
  private static Database other;

  @BeforeAll
  static void setup() {
    // ensure the default server exists first
    db = DB.getDefault();
    other = Database.builder()
      .dataSource(db.pluginApi().dataSource())
      .loadFromProperties()
      .defaultDatabase(false)
      .name("other")
      .ddlGenerate(false)
      .ddlRun(false)
      .build();
  }

  @AfterAll
  static void tearDown() {
    other.shutdown(false, false);
  }

  @Test
  void testBothNear() throws InterruptedException {
    new QFPerson()
      .name.eq("Someone")
      .delete();

    FPerson foo = new FPerson("Someone");
    foo.save();

    DB.cacheManager().clearAll();
    db.metaInfo().resetAllMetrics();
    other.metaInfo().resetAllMetrics();

    FPerson fooA = DB.find(FPerson.class, foo.getId());
    allowAsyncMessaging(); // allow time for background cache load
    FPerson fooB = other.find(FPerson.class, foo.getId());

    DuelCache dualCacheA = db.cacheManager().beanCache(FPerson.class).unwrap(DuelCache.class);
    assertCounts(dualCacheA, 0, 1, 0, 1);
    fooA = DB.find(FPerson.class, foo.getId());
    assertCounts(dualCacheA, 1, 1, 0, 1);
    fooB = other.find(FPerson.class, foo.getId());
    fooA = DB.find(FPerson.class, foo.getId());
    assertCounts(dualCacheA, 2, 1, 0, 1);
    fooB = other.find(FPerson.class, foo.getId());
    DuelCache dualCacheB = other.cacheManager().beanCache(FPerson.class).unwrap(DuelCache.class);
    assertCounts(dualCacheB, 2, 1, 1, 0);
  }

  @Test
  void test() throws InterruptedException {
    for (int i = 0; i < 10; i++) {
      FPerson foo = new FPerson("name " + i);
      foo.save();
    }

    other.cacheManager().clearAll();
    other.metaInfo().resetAllMetrics();

    DuelCache dualCache = other.cacheManager().beanCache(FPerson.class).unwrap(DuelCache.class);

    FPerson foo0 = other.find(FPerson.class, 1);
    assertCounts(dualCache, 0, 1, 0, 1);

    other.find(FPerson.class, 1);
    assertCounts(dualCache, 1, 1, 0, 1);

    other.find(FPerson.class, 1);
    assertCounts(dualCache, 2, 1, 0, 1);

    other.find(FPerson.class, 1);
    assertCounts(dualCache, 3, 1, 0, 1);

    other.find(FPerson.class, 2);
    assertCounts(dualCache, 3, 2, 0, 2);

    foo0.setName("name2");
    foo0.save();
    allowAsyncMessaging();

    FPerson foo3 = other.find(FPerson.class, 1);
    assertThat(foo3.getName()).isEqualTo("name2");
    assertCounts(dualCache, 3, 3, 1, 2);

    foo0.setName("name3");
    foo0.save();
    allowAsyncMessaging();

    foo3 = other.find(FPerson.class, 1);
    assertThat(foo3.getName()).isEqualTo("name3");
    assertCounts(dualCache, 3, 4, 2, 2);
  }

  private void assertCounts(DuelCache dualCache, int nearHits, int nearMiss, int remoteHit, int remoteMiss) {
    assertThat(dualCache.getNearHitCount()).isEqualTo(nearHits);
    assertThat(dualCache.getNearMissCount()).isEqualTo(nearMiss);
    assertThat(dualCache.getRemoteHitCount()).isEqualTo(remoteHit);
    assertThat(dualCache.getRemoteMissCount()).isEqualTo(remoteMiss);
  }

  private void allowAsyncMessaging() throws InterruptedException {
    Thread.sleep(200);
  }
}
