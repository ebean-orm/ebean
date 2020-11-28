package org.tests.model.basic.cache;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.cache.ServerCacheStatistics;
import org.ebeantest.LoggedSqlCollector;
import org.junit.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;

public class TestNatKeyUid extends BaseTestCase {

  private static boolean seededData;
  private static UUID one = UUID.randomUUID();
  private static UUID two = UUID.randomUUID();

  private ServerCacheStatistics appStats() {
    return getBeanCacheStats(OCachedNkeyUid.class, true);
  }

  @Test
  public void test_findOne() {

    setupData();
    clearAllL2Cache();

    OCachedNkeyUid found1 = findOne();
    assertNotNull(found1);
    assertThat(appStats().getHitCount()).isEqualTo(0);

    found1 = findOne();
    assertNotNull(found1);
    assertThat(appStats().getHitCount()).isEqualTo(1);
  }

  private OCachedNkeyUid findOne() {

    return DB.find(OCachedNkeyUid.class)
      .where()
      .eq("cid", one)
      .findOne();
  }

  @Test
  public void test_findMany() {

    setupData();
    clearAllL2Cache();

    List<OCachedNkeyUid> result = findMany();
    assertThat(result).hasSize(2);
    assertThat(appStats().getHitCount()).isEqualTo(0);

    LoggedSqlCollector.start();
    result = findMany();
    assertThat(result).hasSize(2);
    assertThat(appStats().getHitCount()).isEqualTo(2);

    final List<String> sql = LoggedSqlCollector.stop();
    assertThat(sql).as("Expected hit cache, no sql").isEmpty();
  }

  private List<OCachedNkeyUid> findMany() {
    return DB.find(OCachedNkeyUid.class)
      .setUseCache(true)
      .where()
      .in("cid", one, two)
      .findList();
  }

  private void setupData() {
    if (!seededData) {
      seededData = true;
      new OCachedNkeyUid(one, "o1").save();
      new OCachedNkeyUid(two, "o2").save();
    }
  }
}
