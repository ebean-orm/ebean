package org.tests.model.basic.cache;

import io.ebean.xtest.BaseTestCase;
import io.ebean.CacheMode;
import io.ebean.DB;
import io.ebean.cache.ServerCache;
import io.ebean.cache.ServerCacheManager;
import io.ebean.cache.ServerCacheStatistics;
import io.ebean.test.LoggedSql;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestCacheViaComplexNaturalKey extends BaseTestCase {

  private static final Logger log = LoggerFactory.getLogger(TestCacheViaComplexNaturalKey.class);

  private ServerCache beanCache = cacheManager().beanCache(OCachedNatKeyBean.class);
  private ServerCache natKeyCache = cacheManager().naturalKeyCache(OCachedNatKeyBean.class);

  private static boolean loadOnce;

  private ServerCacheManager cacheManager() {
    return server().cacheManager();
  }

  private static void insertSome() {
    if (!loadOnce) {
      DB.find(OCachedNatKeyBean.class).delete();
      for (String store : Arrays.asList("abc", "def")) {
        for (String sku : Arrays.asList("1", "2", "3", "4")) {
          saveBean(store, sku);
        }
      }
      loadOnce = true;
    }
  }

  private static void saveBean(String abc, String sku) {
    DB.save(new OCachedNatKeyBean(abc, sku));
  }

  private void clearCacheAndStatistics() {
    DB.cacheManager().clearAll();
    clearStatistics();
  }

  private void clearStatistics() {
    beanCache.statistics(true);
    natKeyCache.statistics(true);
  }

  private void assertNaturalKeyHitMiss(int expectedHit, int expectedMiss) {
    ServerCacheStatistics stats = natKeyCache.statistics(true);
    assertHitMiss(expectedHit, expectedMiss, stats);
  }

  private void assertBeanCacheHitMiss(int expectedHit, int expectedMiss) {
    ServerCacheStatistics stats = beanCache.statistics(true);
    assertHitMiss(expectedHit, expectedMiss, stats);
  }

  private void assertHitMiss(int expectedHit, int expectedMiss, ServerCacheStatistics stats) {
    assertThat(stats.getHitCount()).isEqualTo(expectedHit);
    assertThat(stats.getMissCount()).isEqualTo(expectedMiss);
  }

  private void setup() {
    insertSome();
    clearCacheAndStatistics();
  }

  private void loadSomeIntoCache() {

    DB.find(OCachedNatKeyBean.class)
      .setBeanCacheMode(CacheMode.PUT)
      .where().le("sku", "2")
      .findList();

    clearStatistics();
  }

  @Test
  public void findList_inClause_someHits() {

    setup();
    loadSomeIntoCache();

    String storeId = "abc";
    List<String> skus = Arrays.asList("1", "2", "3");

    LoggedSql.start();

    log.info("Partial (2 of 3) ...");
    List<OCachedNatKeyBean> list = DB.find(OCachedNatKeyBean.class)
      .where()
      .eq("store", storeId)
      .in("sku", skus)
      .setUseCache(true)
      .orderBy("sku desc")
      .findList();

    List<String> sql = LoggedSql.collect();
    assertThat(sql).hasSize(1);
    if (isH2()) {
      // in clause with only 1 bind param - (sku=3 ... we got hits on sku 1 and 2)
      assertSql(sql.get(0)).contains("from o_cached_natkey t0 where t0.store = ? and t0.sku in (?) order by t0.sku desc; --bind(abc,Array[1]={3})");
    }

    assertThat(list).hasSize(3);
    assertNaturalKeyHitMiss(2, 1);
    assertBeanCacheHitMiss(2, 0);

    list = DB.find(OCachedNatKeyBean.class)
      .where()
      .eq("store", storeId)
      .in("sku", skus)
      .setUseCache(true)
      .orderBy("sku desc")
      .findList();

    sql = LoggedSql.collect();

    assertThat(list).hasSize(3);
    assertThat(sql).hasSize(0);
    assertNaturalKeyHitMiss(3, 0);
    assertBeanCacheHitMiss(3, 0);

    natKeyCache.remove("abc;1;");
    natKeyCache.remove("abc;3;");

    log.info("Partial (1 of 3) ...");
    list = DB.find(OCachedNatKeyBean.class)
      .where()
      .eq("store", storeId)
      .in("sku", skus)
      .setUseCache(true)
      .orderBy("sku desc")
      .findList();

    sql = LoggedSql.stop();

    assertThat(list).hasSize(3);
    assertThat(sql).hasSize(1);
    if (isH2()) {
      assertSql(sql.get(0)).contains("where t0.store = ? and t0.sku in (?,?) order by t0.sku desc; --bind(abc,Array[2]={1,3})");
    }
    assertNaturalKeyHitMiss(1, 2);
    assertBeanCacheHitMiss(1, 0);
  }

  @Test
  public void findList_inClause_allHits() {

    setup();
    loadSomeIntoCache();

    String storeId = "abc";
    List<String> skus = new ArrayList<>(Arrays.asList("1", "2"));

    LoggedSql.start();

    List<OCachedNatKeyBean> list = DB.find(OCachedNatKeyBean.class)
      .where()
      .eq("store", storeId)
      .in("sku", skus)
      .setUseCache(true)
      .orderBy("sku desc")
      .findList();

    List<String> sql = LoggedSql.stop();

    // no SQL - all beans from cache
    assertThat(sql).isEmpty();

    assertThat(list).hasSize(2);
    assertNaturalKeyHitMiss(2, 0);
    assertBeanCacheHitMiss(2, 0);
  }

  @Test
  public void findList_inClause_noHits() {

    setup();
    loadSomeIntoCache();

    String storeId = "abc";
    List<String> skus = new ArrayList<>(Arrays.asList("3", "4"));

    LoggedSql.start();

    List<OCachedNatKeyBean> list = DB.find(OCachedNatKeyBean.class)
      .where()
      .eq("store", storeId)
      .in("sku", skus)
      .setUseCache(true)
      .orderBy("sku desc")
      .findList();

    List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(1);
    if (isH2()) {
      // in clause with 2 bind params as we got not hits on the cache
      assertSql(sql.get(0)).contains("from o_cached_natkey t0 where t0.store = ? and t0.sku in (?,?) order by t0.sku desc; --bind(abc,Array[2]={3,4})");
    }

    assertThat(list).hasSize(2);
    assertNaturalKeyHitMiss(0, 2);
    assertBeanCacheHitMiss(0, 0);
  }

  @Test
  public void hit() {

    setup();
    loadSomeIntoCache();

    // simple case - no IN clause
    OCachedNatKeyBean hit = DB.find(OCachedNatKeyBean.class)
      .where()
      .eq("store", "abc")
      .eq("sku", "2")
      .findOne();

    assertThat(hit).isNotNull();

    assertNaturalKeyHitMiss(1, 0);
    assertBeanCacheHitMiss(1, 0);
  }

  @Test
  public void miss() {

    setup();

    OCachedNatKeyBean miss = DB.find(OCachedNatKeyBean.class)
      .where()
      .eq("store", "def")
      .eq("sku", "4")
      .findOne();

    assertThat(miss).isNotNull();

    // miss on natural key
    assertNaturalKeyHitMiss(0, 1);

    // no activity against bean cache
    assertBeanCacheHitMiss(0, 0);
  }


  @Test
  public void explicitUseCacheFalse_expect_noUseOfCache() {

    setup();

    OCachedNatKeyBean miss = DB.find(OCachedNatKeyBean.class)
      .where()
      .eq("store", "def")
      .eq("sku", "4")
      .setUseCache(false) // explicitly not use cache
      .findOne();

    assertThat(miss).isNotNull();

    // no activity against either cache
    assertNaturalKeyHitMiss(0, 0);
    assertBeanCacheHitMiss(0, 0);
  }

}
