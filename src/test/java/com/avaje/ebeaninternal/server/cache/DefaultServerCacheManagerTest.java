package com.avaje.ebeaninternal.server.cache;

import com.avaje.ebean.cache.ServerCacheFactory;
import com.avaje.ebean.cache.ServerCacheOptions;
import com.avaje.ebean.config.CurrentTenantProvider;
import com.avaje.tests.model.basic.Contact;
import com.avaje.tests.model.basic.Customer;
import org.junit.Test;

import static org.assertj.core.api.StrictAssertions.assertThat;
import static org.junit.Assert.assertTrue;

public class DefaultServerCacheManagerTest {

  private String tenantId;

  private final ServerCacheFactory cacheFactory = new DefaultServerCacheFactory();

  private DefaultServerCacheManager manager = new DefaultServerCacheManager(true, null, cacheFactory, new ServerCacheOptions(), new ServerCacheOptions());

  private DefaultServerCacheManager multiTenantManager = new DefaultServerCacheManager(true, new MyTenantProv(), cacheFactory, new ServerCacheOptions(), new ServerCacheOptions());

  @Test
  public void getCache_normal() throws Exception {


    DefaultServerCache cache = cache(manager, Customer.class);
    assertThat(cache.getName()).isEqualTo("com.avaje.tests.model.basic.Customer_B");

    DefaultServerCache cache1 = cache(manager, Customer.class);
    assertThat(cache1).isSameAs(cache);

    DefaultServerCache cache2 = cache(manager, Contact.class);
    assertThat(cache1).isNotSameAs(cache2);
    assertThat(cache2.getName()).isEqualTo("com.avaje.tests.model.basic.Contact_B");


    DefaultServerCache natKeyCache = (DefaultServerCache) manager.getNaturalKeyCache(Customer.class).get();
    assertThat(natKeyCache.getName()).isEqualTo("com.avaje.tests.model.basic.Customer_N");

    DefaultServerCache queryCache = (DefaultServerCache) manager.getQueryCache(Customer.class).get();
    assertThat(queryCache.getName()).isEqualTo("com.avaje.tests.model.basic.Customer_Q");

    DefaultServerCache collCache = (DefaultServerCache) manager.getCollectionIdsCache(Customer.class, "contacts").get();
    assertThat(collCache.getName()).isEqualTo("com.avaje.tests.model.basic.Customer.contacts_C");
  }

  private DefaultServerCache cache(DefaultServerCacheManager manager, Class<?> beanType) {
    return (DefaultServerCache) manager.getBeanCache(beanType).get();
  }

  @Test
  public void getCache_multiTenant() throws Exception {

    tenantId = "ten1";
    DefaultServerCache cache = cache(multiTenantManager, Customer.class);
    assertThat(cache.getName()).isEqualTo("com.avaje.tests.model.basic.Customer_ten1_B");

    tenantId = "ten2";
    DefaultServerCache cache1 = cache(multiTenantManager, Customer.class);
    assertThat(cache1).isNotSameAs(cache);
    assertThat(cache1.getName()).isEqualTo("com.avaje.tests.model.basic.Customer_ten2_B");
  }

  @Test
  public void isLocalL2Caching() throws Exception {

    assertTrue(manager.isLocalL2Caching());
    assertTrue(multiTenantManager.isLocalL2Caching());
  }


  private class MyTenantProv implements CurrentTenantProvider {

    @Override
    public String currentId() {
      return tenantId;
    }
  }

}
