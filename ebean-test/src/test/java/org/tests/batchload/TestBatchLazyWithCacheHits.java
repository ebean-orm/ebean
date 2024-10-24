package org.tests.batchload;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import io.ebean.cache.ServerCache;
import io.ebean.cache.ServerCacheStatistics;
import io.ebean.test.LoggedSql;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.UUOne;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TestBatchLazyWithCacheHits extends BaseTestCase {

  private final ServerCache beanCache = server().cacheManager().beanCache(UUOne.class);

  private UUOne insert(String name) {
    UUOne one = new UUOne();
    one.setName("testBLWCH" + name);
    DB.save(one);
    return one;
  }

  @Test
  public void testOnCacheHit() {
    List<UUOne> inserted = insertData();
    clearCacheAndStatistics();

    assertNotNull(DB.find(UUOne.class, inserted.get(1).getId()));
    // cache hit
    assertNotNull(DB.find(UUOne.class, inserted.get(1).getId()));
    assertBeanCacheHits(1);

    assertNotNull(DB.find(UUOne.class).where().idEq(inserted.get(2).getId()).findOne());
    // cache hit
    assertNotNull(DB.find(UUOne.class).where().idEq(inserted.get(2).getId()).findOne());
    assertBeanCacheHits(1);

    LoggedSql.start();

    List<UUOne> list = DB.find(UUOne.class)
      .select("id")
      .where().startsWith("name", "testBLWCH")
      .orderBy("name")
      .findList();

    // invoke lazy loading
    for (UUOne uuOne : list) {
      uuOne.getName();
    }
    list.get(0).getName();

    List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(2);
    assertSql(sql.get(0)).contains("from uuone t0 where t0.name like ");
    platformAssertIn(sql.get(1), "from uuone t0 where t0.id");

    // not lazy loading into bean cache
    final ServerCacheStatistics stats = beanCache.statistics(true);
    assertThat(stats.getHitCount()).isEqualTo(10);
    assertThat(stats.getSize()).isEqualTo(10);
  }

  private void assertBeanCacheHits(int hits) {
    ServerCacheStatistics statistics = beanCache.statistics(true);
    assertEquals(statistics.getHitCount(), hits);
  }

  private void clearCacheAndStatistics() {
    beanCache.clear();
    beanCache.statistics(true);
  }

  private List<UUOne> insertData() {
    List<UUOne> inserted = new ArrayList<>();
    String[] names = "A,B,C,D,E,F,G,H,I,J".split(",");
    for (String name : names) {
      inserted.add(insert(name));
    }
    return inserted;
  }

}
