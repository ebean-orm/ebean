package io.ebeaninternal.server.cache;

import io.ebean.config.ContainerConfig;
import io.ebean.config.CurrentTenantProvider;
import io.ebean.config.DatabaseConfig;
import io.ebeaninternal.server.cluster.ClusterManager;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Contact;
import org.tests.model.basic.Customer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DefaultServerCacheManagerTest {

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

  public DefaultServerCacheManagerTest(){
    CacheManagerOptions builder = new CacheManagerOptions(clusterManager, new DatabaseConfig(), true);
    builder.with(new TdTenPro());
    this.multiTenantManager = new DefaultServerCacheManager(builder);
  }


  @Test
  public void getCache_normal() {

    DefaultServerCache cache = cache(manager, Customer.class);
    assertThat(cache.getName()).isEqualTo("org.tests.model.basic.Customer_B");
    assertThat(cache.getShortName()).isEqualTo("Customer_B");

    DefaultServerCache cache1 = cache(manager, Customer.class);
    assertThat(cache1).isSameAs(cache);

    DefaultServerCache cache2 = cache(manager, Contact.class);
    assertThat(cache1).isNotSameAs(cache2);
    assertThat(cache2.getName()).isEqualTo("org.tests.model.basic.Contact_B");
    assertThat(cache2.getShortName()).isEqualTo("Contact_B");


    DefaultServerCache natKeyCache = (DefaultServerCache) manager.getNaturalKeyCache(Customer.class);
    assertThat(natKeyCache.getName()).isEqualTo("org.tests.model.basic.Customer_N");
    assertThat(natKeyCache.getShortName()).isEqualTo("Customer_N");

    DefaultServerCache queryCache = (DefaultServerCache) manager.getQueryCache(Customer.class);
    assertThat(queryCache.getName()).isEqualTo("org.tests.model.basic.Customer_Q");
    assertThat(queryCache.getShortName()).isEqualTo("Customer_Q");

    DefaultServerCache collCache = (DefaultServerCache) manager.getCollectionIdsCache(Customer.class, "contacts");
    assertThat(collCache.getName()).isEqualTo("org.tests.model.basic.Customer.contacts_C");
    assertThat(collCache.getShortName()).isEqualTo("Customer.contacts_C");

    cache.clearCount.reset();
    collCache.clearCount.reset();
    queryCache.clearCount.reset();
    natKeyCache.clearCount.reset();

    manager.clear(Customer.class);

    assertThat(cache.clearCount.get(true)).isEqualTo(1);
    assertThat(natKeyCache.clearCount.get(true)).isEqualTo(1);
    assertThat(queryCache.clearCount.get(true)).isEqualTo(1);
    assertThat(collCache.clearCount.get(true)).isEqualTo(1);
  }

  private DefaultServerCache cache(DefaultServerCacheManager manager, Class<?> beanType) {
    return (DefaultServerCache) manager.getBeanCache(beanType);
  }

  @Test
  public void getCache_multiTenant() {

    tenantId.set("ten1");
    DefaultServerCache cache = cache(multiTenantManager, Customer.class);
    assertThat(cache.getName()).isEqualTo("org.tests.model.basic.Customer_B");
    cache.put("1", "tenant1");

    tenantId.set("ten2");

    assertThat(cache.get("1")).isNull();

    tenantId.set("ten1");

    assertThat(cache.get("1")).isNotNull();

  }

  @Test
  public void getCache_singleTenant() {

    tenantId.set("ten1");
    DefaultServerCache cache = cache(manager, Customer.class);
    assertThat(cache.getName()).isEqualTo("org.tests.model.basic.Customer_B");

    cache.put("1", "tenant1");

    tenantId.set("ten2");

    assertThat(cache.get("1")).isEqualTo("tenant1");

  }

  @Test
  public void isLocalL2Caching() {

    assertTrue(manager.isLocalL2Caching());
    assertTrue(multiTenantManager.isLocalL2Caching());
  }

}
