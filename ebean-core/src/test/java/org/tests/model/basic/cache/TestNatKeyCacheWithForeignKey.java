package org.tests.model.basic.cache;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.cache.ServerCacheStatistics;
import org.ebeantest.LoggedSqlCollector;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestNatKeyCacheWithForeignKey extends BaseTestCase {

  private static boolean seededData;

  private static final OCachedApp app0 = new OCachedApp("app0");
  private static final OCachedApp app1 = new OCachedApp("app1");

  private ServerCacheStatistics getStats() {
    return getBeanCacheStats(OCachedAppDetail.class, true);
  }

  private ServerCacheStatistics appStats() {
    return getBeanCacheStats(OCachedApp.class, true);
  }

  @Test
  public void test_findOne() {

    setupData();
    clearAllL2Cache();

    final OCachedAppDetail found0 = findDetail(app0, "detail0");
    assertThat(found0).isNotNull();
    assertThat(getStats().getHitCount()).isEqualTo(0);

    resetAllMetrics();

    LoggedSqlCollector.start();

    final OCachedAppDetail found1 = findDetail(app0, "detail0");
    assertThat(found1).isNotNull();

    final List<String> sql = LoggedSqlCollector.stop();
    assertThat(sql).as("Expected cache hit, no SQL query expected").isEmpty();
    assertThat(getStats().getHitCount()).isEqualTo(1);
  }

  private OCachedAppDetail findDetail(OCachedApp app, String detail) {

    return DB.find(OCachedAppDetail.class)
      .where()
      .eq("app", app)
      .eq("detail", detail)
      .findOne();
  }


  @Test
  public void test_findList_details_expect_hitNatKeyCache() {

    setupData();
    clearAllL2Cache();

    final List<OCachedAppDetail> result0 = findListDetails(app0, "detail0", "detail1");
    assertThat(result0).hasSize(2);
    assertThat(getStats().getHitCount()).isEqualTo(0);

    LoggedSqlCollector.start();
    final List<OCachedAppDetail> result1 = findListDetails(app0, "detail0", "detail1");
    assertThat(result1).hasSize(2);

    final List<String> sql = LoggedSqlCollector.stop();
    assertThat(sql).as("Expected cache hit, no SQL query expected").isEmpty();
    assertThat(getStats().getHitCount()).isEqualTo(2);
  }

  private List<OCachedAppDetail> findListDetails(OCachedApp app, String... details) {

    return DB.find(OCachedAppDetail.class)
      .setUseCache(true)
      .where()
      .eq("app", app)
      .in("detail", details)
      .findList();
  }

  @Test
  public void test_findList_foreignKey_expect_hitNatKeyCache() {

    setupData();
    clearAllL2Cache();

    final List<OCachedAppDetail> result0 = findListApps("detail0", app0, app1);
    assertThat(result0).hasSize(2);
    assertThat(getStats().getHitCount()).isEqualTo(0);

    LoggedSqlCollector.start();
    final List<OCachedAppDetail> result1 = findListApps("detail0", app0, app1);
    assertThat(result1).hasSize(2);

    final List<String> sql = LoggedSqlCollector.stop();
    assertThat(sql).as("Expected cache hit, no SQL query expected").isEmpty();
    assertThat(getStats().getHitCount()).isEqualTo(2);
  }

  private List<OCachedAppDetail> findListApps(String detail, OCachedApp... apps) {

    return DB.find(OCachedAppDetail.class)
      .setUseCache(true)
      .where()
      .in("app", apps)
      .eq("detail", detail)
      .findList();
  }

  @Test
  public void findSimple() {

    setupData();
    clearAllL2Cache();

    OCachedApp app0 = findAppByName("app0");
    assertThat(app0).isNotNull();
    assertThat(appStats().getHitCount()).isEqualTo(0);

    app0 = findAppByName("app0");
    assertThat(app0).isNotNull();
    assertThat(appStats().getHitCount()).isEqualTo(1);
  }

  private OCachedApp findAppByName(String appName) {

    return DB.find(OCachedApp.class)
      .where()
      .eq("appName", appName)
      .findOne();
  }

  @Test
  public void findApp_many() {

    setupData();
    clearAllL2Cache();

    List<OCachedApp> result = findAppByNames("app0", "app1");
    assertThat(result).hasSize(2);
    assertThat(appStats().getHitCount()).isEqualTo(0);

    result = findAppByNames("app0", "app1");
    assertThat(result).hasSize(2);
    assertThat(appStats().getHitCount()).isEqualTo(2);
  }

  private List<OCachedApp> findAppByNames(String... appNames) {

    return DB.find(OCachedApp.class)
      .setUseCache(true)
      .where()
      .in("appName", appNames)
      .findList();
  }
  private static void setupData() {

    if (!seededData) {
      seededData = true;
      app0.save();
      app1.save();
      new OCachedAppDetail(app0, "detail0").save();
      new OCachedAppDetail(app0, "detail1").save();
      new OCachedAppDetail(app1, "detail0").save();
      new OCachedAppDetail(app1, "detail1").save();
    }
  }
}
