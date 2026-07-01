package org.tests.cache;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import io.ebean.test.LoggedSql;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.OCachedBean;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class testing clearing cache with delete query
 */
public class TestBeanCacheWithDeleteQuery extends BaseTestCase {

  @Test
  public void testBeanCacheGetsClearedOnDelete() {

    OCachedBean bean = new OCachedBean();
    bean.setName("cache-invalidation");
    DB.save(bean);

    OCachedBean bean0 = DB.find(OCachedBean.class, bean.getId());
    assertNotNull(bean0);

    // expect to hit the cache, no SQL
    LoggedSql.start();
    OCachedBean bean1 = DB.find(OCachedBean.class, bean.getId());
    List<String> sql = LoggedSql.stop();
    assertNotNull(bean1);
    assertThat(sql).isEmpty();

    // delete the bean
    int deleted = DB.createQuery(OCachedBean.class).where().eq("name", "cache-invalidation").delete();

    assertEquals(deleted, 1);

    // expect not to hit the cache, expect SQL
    LoggedSql.start();
    OCachedBean bean2 = DB.find(OCachedBean.class).setUnmodifiable(true).setId(String.valueOf(bean.getId())).findOne();
    sql = LoggedSql.stop();
    assertNull(bean2);
    assertThat(sql).isNotEmpty();
  }

  @Test
  public void testBeanCacheGetsClearedOnUpdate() {

    OCachedBean bean = new OCachedBean();
    bean.setName("cache-invalidation-upd");
    DB.save(bean);

    OCachedBean bean0 = DB.find(OCachedBean.class, bean.getId());
    assertNotNull(bean0);

    // expect to hit the cache, no SQL
    LoggedSql.start();
    OCachedBean bean1 = DB.find(OCachedBean.class, bean.getId());
    List<String> sql = LoggedSql.stop();
    assertNotNull(bean1);
    assertThat(sql).isEmpty();

    // update the bean
    int updated = DB.update(OCachedBean.class).set("name", "updatedName").where().eq("name","cache-invalidation-upd").update();

    assertEquals(updated, 1);

    // expect not to hit the cache, expect SQL
    LoggedSql.start();
    OCachedBean bean2 = DB.find(OCachedBean.class).setUnmodifiable(true).setId(String.valueOf(bean.getId())).findOne();
    sql = LoggedSql.stop();
    assertNotNull(bean2);
    assertThat(sql).isNotEmpty();
    assertEquals("updatedName", bean2.getName());
  }

}
