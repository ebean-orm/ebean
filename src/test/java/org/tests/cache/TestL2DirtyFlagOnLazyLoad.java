package org.tests.cache;

import io.ebean.BaseTestCase;
import io.ebean.BeanState;
import io.ebean.Ebean;
import io.ebean.Transaction;
import org.junit.Test;
import org.tests.model.basic.L2CachedLazyDirtFlagResetBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class TestL2DirtyFlagOnLazyLoad extends BaseTestCase {

  @Test
  public void dirtyFlag_reset_after_lazy() {
    L2CachedLazyDirtFlagResetBean bean = new L2CachedLazyDirtFlagResetBean();
    //PREPARE
    try (Transaction tx0 = Ebean.beginTransaction()) {
      bean.setName("findById");
      Ebean.save(bean);
      //bean.save();
      tx0.commit();
    }

    // clearAll() caches via the ServerCacheManager ...
    // Clear all the caches on the default/primary EbeanServer
    server().getServerCacheManager().clearAll();

    //TEST
    try (Transaction tx1 = Ebean.beginTransaction()) {
      // load (and dont touch any related entity)
      L2CachedLazyDirtFlagResetBean bean1 = Ebean.find(L2CachedLazyDirtFlagResetBean.class, bean.getId());
      assertThat(bean1).isNotNull();
      tx1.commit();
    }

    try (Transaction tx2 = Ebean.beginTransaction()) {
      // load (and modify)

      L2CachedLazyDirtFlagResetBean bean2 = Ebean.find(L2CachedLazyDirtFlagResetBean.class, bean.getId());
      assertNotNull(bean2);
      assertEquals("findById", bean2.getName());
      bean2.setName("something");
      BeanState beanState = Ebean.getBeanState(bean2);
      assertTrue(beanState.isDirty());
      //bean2.getChildren().size();
      bean2.someRichObjectMethod();
      assertTrue(beanState.isDirty());

      Ebean.update(bean2);
      tx2.commit();
    }

    try(Transaction tx3 = Ebean.beginTransaction()) {
      // validation

      L2CachedLazyDirtFlagResetBean bean3 = Ebean.find(L2CachedLazyDirtFlagResetBean.class, bean.getId());
      assertEquals("something", bean3.getName());

    }

  }
}
