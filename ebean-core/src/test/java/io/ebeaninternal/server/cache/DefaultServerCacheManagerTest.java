package io.ebeaninternal.server.cache;

import io.ebean.cache.ServerCache;
import io.ebean.config.ContainerConfig;
import io.ebean.config.CurrentTenantProvider;
import io.ebean.config.DatabaseConfig;
import io.ebeaninternal.server.cluster.ClusterManager;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Contact;
import org.tests.model.basic.Customer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultServerCacheManagerTest {

  private final ThreadLocal<String> tenantId = new ThreadLocal<>();

  class TdTenPro implements CurrentTenantProvider {

    @Override
    public Object currentId() {
      return tenantId.get();
    }
  }

  private final ClusterManager clusterManager = new ClusterManager(new ContainerConfig());

  private final DefaultServerCacheManager manager = new DefaultServerCacheManager(new CacheManagerOptions(clusterManager, new DatabaseConfig(), true));

  private final DefaultServerCacheManager multiTenantManager;

  DefaultServerCacheManagerTest(){
    CacheManagerOptions builder = new CacheManagerOptions(clusterManager, new DatabaseConfig(), true);
    builder.with(new TdTenPro());
    this.multiTenantManager = new DefaultServerCacheManager(builder);
  }


  @Test
  void getCache_normal() {
    ServerCache cache = cache(manager, Customer.class);
    DefaultServerCache dsc = cache.unwrap(DefaultServerCache.class);
    assertThat(dsc.getName()).isEqualTo("org.tests.model.basic.Customer_B");
    assertThat(dsc.getShortName()).isEqualTo("Customer_B");

    ServerCache cache1 = cache(manager, Customer.class);
    assertThat(cache1).isSameAs(cache);

    ServerCache cache2 = cache(manager, Contact.class);
    assertThat(cache1).isNotSameAs(cache2);
    DefaultServerCache dsc2 = cache2.unwrap(DefaultServerCache.class);
    assertThat(dsc2.getName()).isEqualTo("org.tests.model.basic.Contact_B");
    assertThat(dsc2.getShortName()).isEqualTo("Contact_B");


    ServerCache natKeyCache = manager.getNaturalKeyCache(Customer.class);
    DefaultServerCache dscNatKey = natKeyCache.unwrap(DefaultServerCache.class);
    assertThat(dscNatKey.getName()).isEqualTo("org.tests.model.basic.Customer_N");
    assertThat(dscNatKey.getShortName()).isEqualTo("Customer_N");

    ServerCache queryCache = manager.getQueryCache(Customer.class);
    DefaultServerCache dscQueryCache = queryCache.unwrap(DefaultServerCache.class);
    assertThat(dscQueryCache.getName()).isEqualTo("org.tests.model.basic.Customer_Q");
    assertThat(dscQueryCache.getShortName()).isEqualTo("Customer_Q");

    ServerCache collCache = manager.getCollectionIdsCache(Customer.class, "contacts");
    DefaultServerCache dscCollCache = collCache.unwrap(DefaultServerCache.class);
    assertThat(dscCollCache.getName()).isEqualTo("org.tests.model.basic.Customer.contacts_C");
    assertThat(dscCollCache.getShortName()).isEqualTo("Customer.contacts_C");

    cache.statistics(true);
    collCache.statistics(true);
    queryCache.statistics(true);
    natKeyCache.statistics(true);

    manager.clear(Customer.class);

    assertThat(cache.statistics(true).getClearCount()).isEqualTo(1);
    assertThat(natKeyCache.statistics(true).getClearCount()).isEqualTo(1);
    assertThat(queryCache.statistics(true).getClearCount()).isEqualTo(1);
    assertThat(collCache.statistics(true).getClearCount()).isEqualTo(1);
  }

  private ServerCache cache(DefaultServerCacheManager manager, Class<?> beanType) {
    return manager.getBeanCache(beanType);
  }

  @Test
  void getCache_multiTenant() {
    tenantId.set("ten1");
    ServerCache cache = cache(multiTenantManager, Customer.class);
    cache.put("1", "tenant1");

    tenantId.set("ten2");
    assertThat(cache.get("1")).isNull();

    tenantId.set("ten1");
    assertThat(cache.get("1")).isEqualTo("tenant1");
  }

  @Test
  void getCache_singleTenant() {
    ServerCache cache = cache(manager, Customer.class);
    tenantId.set("ten1");
    cache.put("1", "tenant1");

    tenantId.set("ten2");
    assertThat(cache.get("1")).isEqualTo("tenant1");
  }

  @Test
  void isLocalL2Caching() {
    assertTrue(manager.isLocalL2Caching());
    assertTrue(multiTenantManager.isLocalL2Caching());
  }

}
