package org.tests.cache;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import org.ebeantest.LoggedSqlCollector;
import org.junit.Test;
import org.tests.model.basic.OCachedBean;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Test class testing clearing cache with delete query
 */
public class TestBeanCacheWithDeleteQuery extends BaseTestCase {

  @Test
  public void testBeanCacheGetsClearedOnDelete() {

    OCachedBean bean = new OCachedBean();
    bean.setName("cache-invalidation");
    Ebean.save(bean);

    OCachedBean bean0 = Ebean.find(OCachedBean.class, bean.getId());
    assertNotNull(bean0);

    // expect to hit the cache, no SQL
    LoggedSqlCollector.start();
    OCachedBean bean1 = Ebean.find(OCachedBean.class, bean.getId());
    List<String> sql = LoggedSqlCollector.stop();
    assertNotNull(bean1);
    assertThat(sql).isEmpty();

    // delete the bean
    int deleted = Ebean.createQuery(OCachedBean.class).where().eq("name", "cache-invalidation").delete();

    assertEquals(deleted, 1);

    // expect not to hit the cache, expect SQL
    LoggedSqlCollector.start();
    OCachedBean bean2 = Ebean.find(OCachedBean.class).setReadOnly(true).setId(String.valueOf(bean.getId())).findOne();
    sql = LoggedSqlCollector.stop();
    assertNull(bean2);
    assertThat(sql).isNotEmpty();
  }

  @Test
  public void testBeanCacheGetsClearedOnUpdate() {

    OCachedBean bean = new OCachedBean();
    bean.setName("cache-invalidation-upd");
    Ebean.save(bean);

    OCachedBean bean0 = Ebean.find(OCachedBean.class, bean.getId());
    assertNotNull(bean0);

    // expect to hit the cache, no SQL
    LoggedSqlCollector.start();
    OCachedBean bean1 = Ebean.find(OCachedBean.class, bean.getId());
    List<String> sql = LoggedSqlCollector.stop();
    assertNotNull(bean1);
    assertThat(sql).isEmpty();

    // update the bean
    int updated = Ebean.update(OCachedBean.class).set("name", "updatedName").where().eq("name","cache-invalidation-upd").update();

    assertEquals(updated, 1);

    // expect not to hit the cache, expect SQL
    LoggedSqlCollector.start();
    OCachedBean bean2 = Ebean.find(OCachedBean.class).setReadOnly(true).setId(String.valueOf(bean.getId())).findOne();
    sql = LoggedSqlCollector.stop();
    assertNotNull(bean2);
    assertThat(sql).isNotEmpty();
    assertEquals("updatedName", bean2.getName());
  }

}
