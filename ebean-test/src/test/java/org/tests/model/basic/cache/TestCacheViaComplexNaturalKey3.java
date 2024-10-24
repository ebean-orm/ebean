package org.tests.model.basic.cache;

import io.ebean.InTuples;
import io.ebean.annotation.Platform;
import io.ebean.service.SpiInTuples;
import io.ebean.xtest.BaseTestCase;
import io.ebean.CacheMode;
import io.ebean.DB;
import io.ebean.Pairs;
import io.ebean.cache.ServerCache;
import io.ebean.cache.ServerCacheManager;
import io.ebean.cache.ServerCacheStatistics;
import io.ebean.test.LoggedSql;
import io.ebean.xtest.IgnorePlatform;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class TestCacheViaComplexNaturalKey3 extends BaseTestCase {

  private ServerCache beanCache = cacheManager().beanCache(OCachedNatKeyBean3.class);
  private ServerCache natKeyCache = cacheManager().naturalKeyCache(OCachedNatKeyBean3.class);

  private static boolean loadOnce;

  private ServerCacheManager cacheManager() {
    return server().cacheManager();
  }

  private static void insertSome() {
    if (!loadOnce) {
      DB.find(OCachedNatKeyBean3.class).delete();

      List<String> stores = asList("abc", "def");
      for (String store : stores) {
        List<String> skus = asList("1", "2", "3");
        for (String sku : skus) {
          int[] codes = {1000,1001,1002,1003,1004};
          for (int code : codes) {
            saveBean(store, code, sku);
          }
        }
      }
      loadOnce = true;
    }
  }

  private static void saveBean(String store, int code, String sku) {
    DB.save(new OCachedNatKeyBean3(store, code, sku));
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

    DB.find(OCachedNatKeyBean3.class)
      .setBeanCacheMode(CacheMode.PUT)
      .where()
      .ge("sku", "2")
      .eq("store", "def")
      .ge("code", 1001)
      .findList();

    clearStatistics();
  }

  @Test
  public void findMap_inClause_allHits() {

    setup();
    loadSomeIntoCache();

    List<Integer> codes = asList(1001);

    LoggedSql.start();

    Map<String,OCachedNatKeyBean3> list = DB.find(OCachedNatKeyBean3.class)
      .where()
      .eq("store", "def")
      .eq("sku", "2")
      .in("code", codes)
      .setUseCache(true)
      .orderBy().asc("code")
      .setMapKey("sku")
      .findMap();

    List<String> sql = LoggedSql.stop();
    assertThat(sql).isEmpty();

    assertThat(list).hasSize(1);

    assertNaturalKeyHitMiss(1, 0);
    assertBeanCacheHitMiss(1, 0);
  }

  @Test
  public void findMap_inClause_someHits() {

    setup();
    loadSomeIntoCache();

    List<Integer> codes = asList(1001, 1000);

    LoggedSql.start();

    Map<String,OCachedNatKeyBean3> list = DB.find(OCachedNatKeyBean3.class)
      .where()
      .eq("store", "def")
      .eq("sku", "2")
      .in("code", codes)
      .setUseCache(true)
      .orderBy().asc("code")
      .setMapKey("sku")
      .findMap();

    List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(1);
    if (isH2()) {
      // in clause with only 1 bind param - miss on 1000
      assertSql(sql.get(0)).contains("from o_cached_natkey3 t0 where t0.store = ? and t0.sku = ? and t0.code in (?) order by t0.code; --bind(def,2,Array[1]={1000})");
    }
    assertThat(list).hasSize(1);

    assertNaturalKeyHitMiss(1, 1);
    assertBeanCacheHitMiss(1, 0);
  }

  @Test
  public void findMap_inClause_noHits() {

    setup();

    LoggedSql.start();

    Map<String,OCachedNatKeyBean3> map = DB.find(OCachedNatKeyBean3.class)
      .where()
      .eq("store", "def")
      .eq("sku", "2")
      .in("code", asList(1002, 1000))
      .setUseCache(true)
      .setMapKey("code")
      .findMap();

    List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(1);
    if (isH2()) {
      // in clause with only 1 bind param - miss on 1000, 1002
      assertSql(sql.get(0)).contains("from o_cached_natkey3 t0 where t0.store = ? and t0.sku = ? and t0.code in (?,?); --bind(def,2,Array[2]={1002,1000})");
    }
    assertThat(map).hasSize(2);

    assertNaturalKeyHitMiss(0, 2);
    assertBeanCacheHitMiss(0, 0);
  }

  @Test
  public void findList_inClause_someHits() {

    setup();
    loadSomeIntoCache();


    List<Integer> codes = asList(1001, 1000, 1002, 1003);

    LoggedSql.start();

    List<OCachedNatKeyBean3> list = DB.find(OCachedNatKeyBean3.class)
      .where()
      .eq("store", "def")
      .in("code", codes)
      .eq("sku", "2")
      .setUseCache(true)
      .orderBy().desc("sku")
      .orderBy().asc("code")
      .findList();

    List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(1);
    if (isH2()) {
      // in clause with only 1 bind param - (sku=3 ... we got hits on sku 1 and 2)
      assertSql(sql.get(0)).contains("from o_cached_natkey3 t0 where t0.store = ? and t0.code in (?) and t0.sku = ? order by t0.sku desc, t0.code; --bind(def,Array[1]={1000},2)");
    }

    assertThat(list).hasSize(4);

    assertNaturalKeyHitMiss(3, 1);
    assertBeanCacheHitMiss(3, 0);
  }

  @Test
  public void findList_inClause_allHits() {

    setup();
    loadSomeIntoCache();

    List<String> skus = asList("2", "3");

    LoggedSql.start();

    List<OCachedNatKeyBean3> list = DB.find(OCachedNatKeyBean3.class)
      .where()
      .eq("store", "def")
      .in("sku", skus)
      .eq("code", 1001)
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
    List<String> skus = asList("3", "2", "4");

    LoggedSql.start();

    List<OCachedNatKeyBean3> list = DB.find(OCachedNatKeyBean3.class)
      .where()
      .eq("store", storeId)
      .in("sku", skus)
      .eq("code", 1001)
      .setUseCache(true)
      .orderBy("sku desc")
      .findList();

    List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(1);
    if (isH2()) {
      // in clause with 2 bind params as we got not hits on the cache
      assertSql(sql.get(0)).contains("from o_cached_natkey3 t0 where t0.store = ? and t0.sku in (?,?,?) and t0.code = ? order by t0.sku desc; --bind(abc,Array[3]={3,2,4},1001)");
    }

    assertThat(list).hasSize(2);
    assertNaturalKeyHitMiss(0, 3);
    assertBeanCacheHitMiss(0, 0);
  }


  @Test
  public void hit() {

    setup();
    loadSomeIntoCache();

    // simple case - no IN clause
    OCachedNatKeyBean3 hit = DB.find(OCachedNatKeyBean3.class)
      .where()
      .eq("store", "def")
      .eq("sku", "2")
      .eq("code", 1002)
      .findOne();

    assertThat(hit).isNotNull();

    assertNaturalKeyHitMiss(1, 0);
    assertBeanCacheHitMiss(1, 0);
  }

  @Test
  public void miss() {

    setup();

    OCachedNatKeyBean3 miss = DB.find(OCachedNatKeyBean3.class)
      .where()
      .eq("store", "abc")
      .eq("sku", "2")
      .eq("code", 1000)
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

    OCachedNatKeyBean3 miss = DB.find(OCachedNatKeyBean3.class)
      .where()
      .eq("store", "abc")
      .eq("sku", "2")
      .eq("code", 1000)
      .setUseCache(false) // explicitly not use cache
      .findOne();

    assertThat(miss).isNotNull();

    // no activity against either cache
    assertNaturalKeyHitMiss(0, 0);
    assertBeanCacheHitMiss(0, 0);
  }

  @Test
  public void findList_inPairs_standardConcat() {

    setup();
    loadSomeIntoCache();

    Pairs pairs = new Pairs("sku", "code")
      .add("2", 1000)
      .add("2", 1001)
      .add("3", 1000);

    LoggedSql.start();

    List<OCachedNatKeyBean3> list = DB.find(OCachedNatKeyBean3.class)
      .where()
      .eq("store", "def")
      .inPairs(pairs)
      .setUseCache(true)
      .orderBy("sku desc")
      .findList();

    List<String> sql = LoggedSql.stop();

    assertThat(pairs.entries()).hasSize(3);

    assertThat(list).hasSize(3);
    assertNaturalKeyHitMiss(1, 2);
    assertBeanCacheHitMiss(1, 0);

    if (isH2()) {
      assertSql(sql.get(0)).contains("from o_cached_natkey3 t0 where t0.store = ? and concat(t0.sku,'-',t0.code) in (?,?) order by t0.sku desc; --bind(def,Array[2]={2-1000,3-1000})");
    } else if (isPostgresCompatible() || isOracle() || isDb2()) {
      assertSql(sql.get(0)).contains("from o_cached_natkey3 t0 where t0.store = ? and (t0.sku||'-'||t0.code)");
    } else if (isHana()) {
      assertSql(sql.get(0)).contains("from o_cached_natkey3 t0 where t0.store = ? and concat(t0.sku, '-'||t0.code)");
    } else {
      assertSql(sql.get(0)).contains("from o_cached_natkey3 t0 where t0.store = ? and concat(t0.sku,'-',t0.code)");
    }

  }

  @Test
  public void findList_inPairs_userConcat() {

    setup();
    loadSomeIntoCache();

    Pairs pairs = new Pairs("sku", "code")
      .concatSeparator(":")
      .concatSuffix("-foo")
      .add("2", 1000)
      .add("2", 1001)
      .add("3", 1000);

    LoggedSql.start();

    List<OCachedNatKeyBean3> list = DB.find(OCachedNatKeyBean3.class)
      .where()
      .eq("store", "def")
      .inPairs(pairs)
      .setBeanCacheMode(CacheMode.ON)
      .orderBy("sku desc")
      .findList();

    List<String> sql = LoggedSql.stop();

    assertThat(pairs.entries()).hasSize(3);

    assertThat(list).hasSize(3);
    assertNaturalKeyHitMiss(1, 2);
    assertBeanCacheHitMiss(1, 0);

    if (isH2()) {
      assertSql(sql.get(0)).contains("from o_cached_natkey3 t0 where t0.store = ? and concat(t0.sku,':',t0.code,'-foo') in (?,?) order by t0.sku desc; --bind(def,Array[2]={2:1000-foo,3:1000-foo})");
    } else if (isPostgresCompatible() || isOracle() || isDb2()){
      assertSql(sql.get(0)).contains("from o_cached_natkey3 t0 where t0.store = ? and (t0.sku||':'||t0.code||'-foo')");
    } else if (isHana()){
      assertSql(sql.get(0)).contains("from o_cached_natkey3 t0 where t0.store = ? and concat(t0.sku, ':'||t0.code||'-foo')");
    } else {
      assertSql(sql.get(0)).contains("from o_cached_natkey3 t0 where t0.store = ? and concat(t0.sku,':',t0.code,'-foo')");
    }
  }

  @IgnorePlatform({Platform.SQLSERVER, Platform.DB2})
  @Test
  public void findList_inTuples_noCache() {
    setup();

    InTuples tuples = InTuples.of("sku", "code")
      .add("2", 1000)
      .add("2", 1001)
      .add("3", 1000);

    LoggedSql.start();

    List<OCachedNatKeyBean3> list = DB.find(OCachedNatKeyBean3.class)
      .where()
      .eq("store", "def")
      .inTuples(tuples)
      .setUseCache(false)
      .orderBy("sku desc")
      .findList();

    List<String> sql = LoggedSql.stop();

    SpiInTuples spiTuples = (SpiInTuples)tuples;
    assertThat(spiTuples.properties()).containsOnly("sku", "code");
    assertThat(spiTuples.entries()).hasSize(3);
    assertThat(list).hasSize(3);
    assertSql(sql.get(0)).contains("from o_cached_natkey3 t0 where t0.store = ? and (t0.sku,t0.code) in ((?,?),(?,?),(?,?)) order by t0.sku desc;");
  }
}
