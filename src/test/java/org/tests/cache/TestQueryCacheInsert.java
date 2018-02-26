package org.tests.cache;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.EbeanServer;
import io.ebean.cache.ServerCache;
import io.ebean.cache.ServerCacheStatistics;
import org.junit.Test;
import org.tests.model.basic.EBasicVer;

import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class TestQueryCacheInsert extends BaseTestCase {

  @Test
  public void test() {

    EbeanServer server = Ebean.getServer(null);

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

    Ebean.save(doda);

    ServerCache queryCache = Ebean.getServerCacheManager().getQueryCache(EBasicVer.class);
    queryCache.getStatistics(true);

    Optional<EBasicVer> found0 = Ebean.find(EBasicVer.class)
      .where().eq("description", "OddButUniqueSillyExample")
      .setUseQueryCache(true)
      .findOneOrEmpty();

    assertTrue(found0.isPresent());
    assertHitMiss(0, 1, queryCache);

    EBasicVer found1 = Ebean.find(EBasicVer.class)
      .where().eq("description", "OddButUniqueSillyExample")
      .setUseQueryCache(true)
      .findOne();

    assertNotNull(found1);
    assertHitMiss(1, 0, queryCache);
  }

  private void assertHitMiss(int hits, int miss, ServerCache queryCache) {
    ServerCacheStatistics stats = queryCache.getStatistics(true);
    assertEquals(hits, stats.getHitCount());
    assertEquals(miss, stats.getMissCount());
  }
}
