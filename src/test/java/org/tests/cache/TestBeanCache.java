package org.tests.cache;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.Ebean;
import io.ebean.cache.ServerCache;
import io.ebean.cache.ServerCacheStatistics;
import org.ebeantest.LoggedSqlCollector;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tests.model.basic.Country;
import org.tests.model.basic.OCachedBean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;

/**
 * Test class testing deleting/invalidating of cached beans
 */
public class TestBeanCache extends BaseTestCase {

  private static final Logger log = LoggerFactory.getLogger(TestBeanCache.class);

  private ServerCache beanCache = DB.getDefault().getServerCacheManager().getBeanCache(OCachedBean.class);

  @Test
  public void findById_when_idTypeConverted() {

    OCachedBean bean = new OCachedBean();
    bean.setName("findById");
    Ebean.save(bean);

    OCachedBean bean0 = Ebean.find(OCachedBean.class, bean.getId());
    assertNotNull(bean0);

    // expect to hit the cache, no SQL
    LoggedSqlCollector.start();
    OCachedBean bean1 = Ebean.find(OCachedBean.class, bean.getId());
    List<String> sql = LoggedSqlCollector.stop();
    assertNotNull(bean1);
    assertThat(sql).isEmpty();

    // expect to hit the cache, no SQL
    LoggedSqlCollector.start();
    OCachedBean bean2 = Ebean.find(OCachedBean.class).setReadOnly(true).setId(String.valueOf(bean.getId())).findOne();
    sql = LoggedSqlCollector.stop();
    assertNotNull(bean2);
    assertThat(sql).isEmpty();
  }

  @Test
  public void idsInExpression() {

    List<OCachedBean> beans = createBeans();
    List<Long> ids = beans.stream().map(OCachedBean::getId).collect(Collectors.toList());

    beanCache.clear();
    beanCache.getStatistics(true);

    LoggedSqlCollector.start();

    log.info("All misses (0 of 3) ...");
    List<OCachedBean> list = DB.find(OCachedBean.class)
      .where().idIn(ids)
      .setUseCache(true)
      .findList();

    assertThat(list).hasSize(3);
    assertBeanCacheHitMiss(0, 3);
    List<String> sql = LoggedSqlCollector.current();
    assertThat(sql).hasSize(1);
    if (isH2()) {
      assertThat(sql.get(0)).contains("from o_cached_bean t0 where t0.id in (?,?,?)");
    }

    log.info("All hits (3 of 3) ...");
    list = DB.find(OCachedBean.class)
      .where().idIn(ids)
      .setUseCache(true)
      .findList();

    assertBeanCacheHitMiss(3, 0);
    assertThat(list).hasSize(3);
    sql = LoggedSqlCollector.current();
    assertThat(sql).hasSize(0); // no misses

    // remove a bean so that we get a "partial" hit (2 out of 3 in cache)
    beanCache.remove(beans.get(0).getId().toString());

    log.info("Partial hits (2 of 3) ...");
    list = DB.find(OCachedBean.class)
      .where().idIn(ids)
      .setUseCache(true)
      .findList();

    assertBeanCacheHitMiss(2, 1);
    assertThat(list).hasSize(3);
    sql = LoggedSqlCollector.current();
    assertThat(sql).hasSize(1);
    if (isH2()) {
      // fetch the miss from DB
      assertThat(sql.get(0)).contains("from o_cached_bean t0 where t0.id in (?)");
    }

    // remove beans so that we get a "partial" hit (1 out of 3 in cache)
    beanCache.remove(beans.get(1).getId().toString());
    beanCache.remove(beans.get(2).getId().toString());

    log.info("Partial hits (1 of 3) ...");
    list = DB.find(OCachedBean.class)
      .where().idIn(ids)
      .setUseCache(true)
      .findList();

    assertBeanCacheHitMiss(1, 2);
    assertThat(list).hasSize(3);
    sql = LoggedSqlCollector.stop();
    assertThat(sql).hasSize(1);
    if (isH2()) {
      // fetch the misses from DB
      assertThat(sql.get(0)).contains("from o_cached_bean t0 where t0.id in (?,?)");
    }
  }

  private void assertBeanCacheHitMiss(int hitCount, int missCount) {
    ServerCacheStatistics statistics = beanCache.getStatistics(true);
    assertThat(statistics.getHitCount()).isEqualTo(hitCount);
    assertThat(statistics.getMissCount()).isEqualTo(missCount);
  }

  private List<OCachedBean> createBeans() {

    List<String> names = Arrays.asList("z0", "z1", "z2");

    List<OCachedBean> beans = new ArrayList<>();
    for (String name : names) {
      OCachedBean bean = new OCachedBean();
      bean.setName(name);
      beans.add(bean);
    }

    DB.saveAll(beans);

    return beans;
  }

  @Test
  public void find_whenNotExits() {

    Country country = Ebean.find(Country.class)
      .where()
      .eq("name","NotValid")
      .findOne();

    assertThat(country).isNull();
  }
}
