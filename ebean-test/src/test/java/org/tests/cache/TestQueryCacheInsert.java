package org.tests.cache;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.Database;
import io.ebean.cache.ServerCache;
import io.ebean.cache.ServerCacheStatistics;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.EBasicVer;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class TestQueryCacheInsert extends BaseTestCase {

  @Test
  public void test() {

    Database server = DB.getDefault();

    EBasicVer account = new EBasicVer("junk");
    server.save(account);

    List<EBasicVer> alist0 = server.find(EBasicVer.class).setUseQueryCache(true).findList();

    EBasicVer a2 = new EBasicVer("junk2");
    server.save(a2);
    awaitL2Cache();

    List<EBasicVer> alist1 = server.find(EBasicVer.class).setUseQueryCache(true).findList();

    assertEquals(alist0.size() + 1, alist1.size());
  }

  @Test
  public void findOne() {

    EBasicVer doda = new EBasicVer("doda");
    doda.setDescription("OddButUniqueSillyExample");

    DB.save(doda);

    ServerCache queryCache = DB.cacheManager().queryCache(EBasicVer.class);
    queryCache.statistics(true);

    Optional<EBasicVer> found0 = DB.find(EBasicVer.class)
      .where().eq("description", "OddButUniqueSillyExample")
      .setUseQueryCache(true)
      .findOneOrEmpty();

    assertTrue(found0.isPresent());
    assertHitMiss(0, 1, queryCache);

    EBasicVer found1 = DB.find(EBasicVer.class)
      .where().eq("description", "OddButUniqueSillyExample")
      .setUseQueryCache(true)
      .findOne();

    assertNotNull(found1);
    assertHitMiss(1, 0, queryCache);
  }

  private void assertHitMiss(int hits, int miss, ServerCache queryCache) {
    ServerCacheStatistics stats = queryCache.statistics(true);
    assertEquals(hits, stats.getHitCount());
    assertEquals(miss, stats.getMissCount());
  }
}
