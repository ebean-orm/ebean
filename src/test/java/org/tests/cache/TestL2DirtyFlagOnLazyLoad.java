package org.tests.cache;

import org.tests.model.basic.L2CachedLazyDirtFlagResetBean;
import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.Transaction;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TestL2DirtyFlagOnLazyLoad extends BaseTestCase {

  @Test
  public void dirtyFlag_reset_after_lazy() {

    //PREPARE
    Transaction tx0 = Ebean.beginTransaction();
      L2CachedLazyDirtFlagResetBean bean = new L2CachedLazyDirtFlagResetBean();
      bean.setName("findById");
      Ebean.save(bean);
      //bean.save();
    tx0.commit();

    // clearAll() caches via the ServerCacheManager ...
    // Clear all the caches on the default/primary EbeanServer
    server().getServerCacheManager().clearAll();

    //TEST
    Transaction tx1 = Ebean.beginTransaction();
      // load (and dont touch any related entity)
      L2CachedLazyDirtFlagResetBean bean1 = Ebean.find(L2CachedLazyDirtFlagResetBean.class, bean.getId());
    tx1.commit();


    Transaction tx2 = Ebean.beginTransaction();
      // load (and modify)

      L2CachedLazyDirtFlagResetBean bean2 = Ebean.find(L2CachedLazyDirtFlagResetBean.class, bean.getId());
      assertNotNull(bean2);
      assertEquals("findById", bean2.getName());
      bean2.setName("something");
      bean2.someRichObjectMethod();

      Ebean.update(bean2);
    tx2.commit();

    Transaction tx3 = Ebean.beginTransaction();
      // validation

      L2CachedLazyDirtFlagResetBean bean3 = Ebean.find(L2CachedLazyDirtFlagResetBean.class, bean.getId());
      assertEquals("something", bean3.getName());

    tx3.rollback();

  }
}
