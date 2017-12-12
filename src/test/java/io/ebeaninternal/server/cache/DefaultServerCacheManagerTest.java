package io.ebeaninternal.server.cache;

import io.ebean.config.ContainerConfig;
import io.ebean.config.CurrentTenantProvider;
import io.ebean.config.ServerConfig;
import io.ebeaninternal.server.cluster.ClusterManager;
import org.junit.Test;
import org.tests.model.basic.Contact;
import org.tests.model.basic.Customer;

import static org.assertj.core.api.StrictAssertions.assertThat;
import static org.junit.Assert.assertTrue;

public class DefaultServerCacheManagerTest {

  private ThreadLocal<String> tenantId = new ThreadLocal<>();

  class TdTenPro implements CurrentTenantProvider {

    @Override
    public Object currentId() {
      return tenantId.get();
    }
  }

  private ClusterManager clusterManager = new ClusterManager(new ContainerConfig());

  private DefaultServerCacheManager manager = new DefaultServerCacheManager(new CacheManagerOptions(clusterManager, new ServerConfig(), true));

  private DefaultServerCacheManager multiTenantManager;

  public DefaultServerCacheManagerTest(){

    CacheManagerOptions builder = new CacheManagerOptions(clusterManager, new ServerConfig(), true);
    builder.with(new TdTenPro());

    this.multiTenantManager = new DefaultServerCacheManager(builder);
  }


  @Test
  public void getCache_normal() {

    DefaultServerCache cache = cache(manager, Customer.class);
    assertThat(cache.getName()).isEqualTo("org.tests.model.basic.Customer_B");

    DefaultServerCache cache1 = cache(manager, Customer.class);
    assertThat(cache1).isSameAs(cache);

    DefaultServerCache cache2 = cache(manager, Contact.class);
    assertThat(cache1).isNotSameAs(cache2);
    assertThat(cache2.getName()).isEqualTo("org.tests.model.basic.Contact_B");


    DefaultServerCache natKeyCache = (DefaultServerCache) manager.getNaturalKeyCache(Customer.class);
    assertThat(natKeyCache.getName()).isEqualTo("org.tests.model.basic.Customer_N");

    DefaultServerCache queryCache = (DefaultServerCache) manager.getQueryCache(Customer.class);
    assertThat(queryCache.getName()).isEqualTo("org.tests.model.basic.Customer_Q");

    DefaultServerCache collCache = (DefaultServerCache) manager.getCollectionIdsCache(Customer.class, "contacts");
    assertThat(collCache.getName()).isEqualTo("org.tests.model.basic.Customer.contacts_C");

    cache.clearCount.sumThenReset();
    collCache.clearCount.sumThenReset();
    queryCache.clearCount.sumThenReset();
    natKeyCache.clearCount.sumThenReset();

    manager.clear(Customer.class);

    assertThat(cache.clearCount.sumThenReset()).isEqualTo(1);
    assertThat(natKeyCache.clearCount.sumThenReset()).isEqualTo(1);
    assertThat(queryCache.clearCount.sumThenReset()).isEqualTo(1);
    assertThat(collCache.clearCount.sumThenReset()).isEqualTo(1);
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
