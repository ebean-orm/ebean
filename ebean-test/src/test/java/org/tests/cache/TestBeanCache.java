package org.tests.cache;

import io.ebean.DB;
import io.ebean.Query;
import io.ebean.Transaction;
import io.ebean.UnmodifiableEntityException;
import io.ebean.cache.ServerCache;
import io.ebean.cache.ServerCacheStatistics;
import io.ebean.test.LoggedSql;
import io.ebean.xtest.BaseTestCase;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tests.model.basic.Country;
import org.tests.model.basic.OCachedBean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Test class testing deleting/invalidating of cached beans
 */
public class TestBeanCache extends BaseTestCase {

  private static final Logger log = LoggerFactory.getLogger(TestBeanCache.class);

  private final ServerCache beanCache = DB.getDefault().cacheManager().beanCache(OCachedBean.class);

  @Test
  public void findById_when_idTypeConverted() {

    OCachedBean bean = new OCachedBean();
    bean.setName("findById");
    DB.save(bean);

    OCachedBean bean0 = DB.find(OCachedBean.class, bean.getId());
    assertNotNull(bean0);

    // expect to hit the cache, no SQL
    LoggedSql.start();
    OCachedBean bean1 = DB.find(OCachedBean.class, bean.getId());
    List<String> sql = LoggedSql.stop();
    assertNotNull(bean1);
    assertThat(sql).isEmpty();

    // expect to hit the cache, no SQL
    LoggedSql.start();
    OCachedBean bean2 = DB.find(OCachedBean.class).setReadOnly(true).setId(String.valueOf(bean.getId())).findOne();
    sql = LoggedSql.stop();
    assertNotNull(bean2);
    assertThat(sql).isEmpty();
  }

  @Test
  public void idsInFindMap() {

    List<OCachedBean> beans = createBeans(Arrays.asList("m0", "m1", "m2", "m3", "m4", "m5", "m6"));
    List<Long> ids = beans.stream().map(OCachedBean::getId).collect(Collectors.toList());
    beanCache.clear();
    beanCache.statistics(true);
    Query<OCachedBean> query = DB.find(OCachedBean.class).setUseCache(true);

    // Test findIds
    LoggedSql.start();
    query.copy()
      .where().idIn(ids.subList(0, 1))
      .findMap(); // cache key is: 3/d[{/c1000}]/w[List[IdIn[?1],]]
    if (isPostgresCompatible()) {
      assertThat(LoggedSql.stop().get(0)).contains("t0.id = any(?)");
    } else {
      assertThat(LoggedSql.stop().get(0)).contains("in (?)");
    }

    LoggedSql.start();
    query.copy()
      .where().idIn(ids.subList(0, 4))
      .findMap(); // cache key is: 3/d[{/c1000}]/w[List[IdIn[?5],]]
    if (isPostgresCompatible()) {
      assertThat(LoggedSql.stop().get(0)).contains("t0.id = any(?)");
    } else {
      assertThat(LoggedSql.stop().get(0)).contains("in (?,?,?,?,?)");
    }

    LoggedSql.start();
    query.copy()
      .where().idIn(ids.subList(2, 6))
      .findMap(); // same cache key as above and same SQL above
    if (isPostgresCompatible()) {
      assertThat(LoggedSql.stop().get(0)).contains("t0.id = any(?)");
    } else {
      assertThat(LoggedSql.stop().get(0)).contains("in (?,?,?,?,?)");
    }
  }

  @Test
  public void idsIn_explicitCache_expect_cachePut() {

    List<OCachedBean> beans = createBeans(Arrays.asList("k0", "k1"));
    List<Long> ids = beans.stream().map(OCachedBean::getId).collect(Collectors.toList());

    beanCache.clear();
    beanCache.statistics(true);
    try (Transaction transaction = DB.beginTransaction()) {

      // skipCacheAfterWrite set after this write ...
      final OCachedBean junk = createBean("junk");
      DB.save(junk);

      List<OCachedBean> list = DB.find(OCachedBean.class)
        .where().idIn(ids)
        .setUseCache(true)
        .findList();

      assertThat(list).hasSize(2);
      transaction.commit();
    }

    ServerCacheStatistics statistics = beanCache.statistics(true);
    assertThat(statistics.getHitCount()).isEqualTo(0);
    assertThat(statistics.getMissCount()).isEqualTo(0);
    assertThat(statistics.getPutCount()).isEqualTo(2);
  }

  @Test
  public void idsInExpression() {

    List<OCachedBean> beans = createBeans(Arrays.asList("z0", "z1", "z2"));
    List<Long> ids = beans.stream().map(OCachedBean::getId).collect(Collectors.toList());

    beanCache.clear();
    beanCache.statistics(true);

    LoggedSql.start();

    log.info("All misses (0 of 3) ...");
    final List<OCachedBean> list1 = DB.find(OCachedBean.class)
      .where().idIn(ids)
      .setUseCache(true)
      .setUnmodifiable(true)
      .findList();

    assertThat(list1).hasSize(3);
    assertThatThrownBy(() -> list1.get(0).setName("junk")).isInstanceOf(UnmodifiableEntityException.class);

    assertBeanCacheHitMiss(0, 3);
    List<String> sql = LoggedSql.collect();
    assertThat(sql).hasSize(1);
    if (isH2()) {
      assertSql(sql.get(0)).contains("from o_cached_bean t0 where t0.id in (?,?,?,?,?)");
    }

    log.info("All hits (3 of 3) ...");
    List<OCachedBean> list2 = DB.find(OCachedBean.class)
      .where().idIn(ids)
      .setUseCache(true)
      .setUnmodifiable(true)
      .findList();

    assertBeanCacheHitMiss(3, 0);
    assertThat(list2).hasSize(3);
    assertThatThrownBy(() -> list2.add(new OCachedBean())).isInstanceOf(UnsupportedOperationException.class);
    assertThatThrownBy(() -> list2.get(0).setName("junk")).isInstanceOf(UnmodifiableEntityException.class);

    sql = LoggedSql.collect();
    assertThat(sql).hasSize(0); // no misses

    // remove a bean so that we get a "partial" hit (2 out of 3 in cache)
    beanCache.remove(beans.get(0).getId().toString());

    log.info("Partial hits (2 of 3) ...");
    List<OCachedBean> list3 = DB.find(OCachedBean.class)
      .where().idIn(ids)
      .setUseCache(true)
      .findList();

    assertBeanCacheHitMiss(2, 1);
    assertThat(list3).hasSize(3);
    list3.get(0).setName("junk"); // we can mutate the beans
    list3.clear(); // we can mutate the list

    sql = LoggedSql.collect();
    assertThat(sql).hasSize(1);
    if (isH2()) {
      // fetch the miss from DB
      assertSql(sql.get(0)).contains("from o_cached_bean t0 where t0.id in (?)");
    }

    // remove beans so that we get a "partial" hit (1 out of 3 in cache)
    beanCache.remove(beans.get(1).getId().toString());
    beanCache.remove(beans.get(2).getId().toString());

    log.info("Partial hits (1 of 3) ...");
    List<OCachedBean> list4 = DB.find(OCachedBean.class)
      .where().idIn(ids)
      .setUseCache(true)
      .findList();

    assertBeanCacheHitMiss(1, 2);
    assertThat(list4).hasSize(3);
    sql = LoggedSql.stop();
    assertThat(sql).hasSize(1);
    if (isH2()) {
      // fetch the misses from DB
      assertSql(sql.get(0)).contains("from o_cached_bean t0 where t0.id in (?,?,?,?,?)");
    }
  }

  @Test
  public void testFindSet() {

    List<OCachedBean> beans = createBeans(Arrays.asList("z0", "z1", "z2"));
    List<Long> ids = beans.stream().map(OCachedBean::getId).collect(Collectors.toList());

    beanCache.clear();
    beanCache.statistics(true);

    LoggedSql.start();

    log.info("All misses (0 of 3) ...");
    Set<OCachedBean> set = DB.find(OCachedBean.class)
      .where().idIn(ids)
      .setUseCache(true)
      .findSet();

    assertThat(set).hasSize(3);
    assertBeanCacheHitMiss(0, 3);
    List<String> sql = LoggedSql.collect();
    assertThat(sql).hasSize(1);
    if (isH2()) {
      assertSql(sql.get(0)).contains("from o_cached_bean t0 where t0.id in (?,?,?,?,?)");
    }

    log.info("All hits (3 of 3) ...");
    set = DB.find(OCachedBean.class)
      .where().idIn(ids)
      .setUseCache(true)
      .findSet();

    assertBeanCacheHitMiss(3, 0);
    assertThat(set).hasSize(3);
    sql = LoggedSql.collect();
    assertThat(sql).hasSize(0); // no misses

    // remove a bean so that we get a "partial" hit (2 out of 3 in cache)
    beanCache.remove(beans.get(0).getId().toString());

    log.info("Partial hits (2 of 3) ...");
    set = DB.find(OCachedBean.class)
      .where().idIn(ids)
      .setUseCache(true)
      .findSet();

    assertBeanCacheHitMiss(2, 1);
    assertThat(set).hasSize(3);
    sql = LoggedSql.collect();
    assertThat(sql).hasSize(1);
    if (isH2()) {
      // fetch the miss from DB
      assertSql(sql.get(0)).contains("from o_cached_bean t0 where t0.id in (?)");
    }

    // remove beans so that we get a "partial" hit (1 out of 3 in cache)
    beanCache.remove(beans.get(1).getId().toString());
    beanCache.remove(beans.get(2).getId().toString());

    log.info("Partial hits (1 of 3) ...");
    set = DB.find(OCachedBean.class)
      .where().idIn(ids)
      .setUseCache(true)
      .findSet();

    assertBeanCacheHitMiss(1, 2);
    assertThat(set).hasSize(3);
    sql = LoggedSql.stop();
    assertThat(sql).hasSize(1);
    if (isH2()) {
      // fetch the misses from DB
      assertSql(sql.get(0)).contains("from o_cached_bean t0 where t0.id in (?,?,?,?,?)");
    }
  }

  private void assertBeanCacheHitMiss(int hitCount, int missCount) {
    ServerCacheStatistics statistics = beanCache.statistics(true);
    assertThat(statistics.getHitCount()).isEqualTo(hitCount);
    assertThat(statistics.getMissCount()).isEqualTo(missCount);
  }

  private List<OCachedBean> createBeans(List<String> names) {
    List<OCachedBean> beans = new ArrayList<>();
    for (String name : names) {
      beans.add(createBean(name));
    }
    DB.saveAll(beans);
    return beans;
  }

  private OCachedBean createBean(String name) {
    OCachedBean bean = new OCachedBean();
    bean.setName(name);
    return bean;
  }

  @Test
  public void find_whenNotExits() {

    Country country = DB.find(Country.class)
      .where()
      .eq("name", "NotValid")
      .findOne();

    assertThat(country).isNull();
  }
}
