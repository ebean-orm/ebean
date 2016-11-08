package com.avaje.tests.cache;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.cache.ServerCacheManager;
import com.avaje.tests.model.basic.Customer;
import com.avaje.tests.model.basic.ResetBasicData;
import org.junit.Assert;
import org.junit.Test;

public class TestCacheReferenceLazyLoad extends BaseTestCase {

  @Test
  public void testLazyLoadOnRef() {

    ResetBasicData.reset();

    ServerCacheManager serverCacheManager = Ebean.getServerCacheManager();
    boolean originallyBeanCaching = serverCacheManager.isBeanCaching(Customer.class);

    Customer customer = Ebean.getReference(Customer.class, 1);

    // invoke lazy loading
    customer.getName();

    if (!originallyBeanCaching) {
      // the lazy loading shouldn't start the L2 bean cache
      boolean beanCachingAfter = serverCacheManager.isBeanCaching(Customer.class);
      Assert.assertFalse(beanCachingAfter);
    }
  }

}
