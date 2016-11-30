package com.avaje.ebeaninternal.server.cache;

import com.avaje.ebean.cache.ServerCache;
import com.avaje.ebean.cache.ServerCacheFactory;
import com.avaje.ebean.cache.ServerCacheOptions;
import com.avaje.ebean.cache.ServerCacheType;
import com.avaje.ebean.config.CurrentTenantProvider;
import com.avaje.tests.model.basic.Contact;
import com.avaje.tests.model.basic.Customer;
import org.junit.Test;

import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;


public class DefaultCacheHolderTest {

  private String tenantId;

  private final ServerCacheFactory cacheFactory = new DefaultServerCacheFactory();
  private final ServerCacheOptions defaultOptions = new ServerCacheOptions();

  @Test
  public void getCache_normal() throws Exception {

    DefaultCacheHolder holder = new DefaultCacheHolder(cacheFactory, defaultOptions, defaultOptions, null);

    DefaultServerCache cache = cache(holder, Customer.class, "customer");
    assertThat(cache.getName()).isEqualTo("customer_B");

    DefaultServerCache cache1 = cache(holder, Customer.class, "customer");
    assertThat(cache1).isSameAs(cache);

    DefaultServerCache cache2 = cache(holder, Contact.class, "contact");
    assertThat(cache1).isNotSameAs(cache2);
    assertThat(cache2.getName()).isEqualTo("contact_B");
  }

  private DefaultServerCache cache(DefaultCacheHolder holder, Class<?> type, String name) {
    Supplier<ServerCache> cache = holder.getCache(type, name, ServerCacheType.BEAN);
    return (DefaultServerCache) cache.get();
  }

  @Test
  public void getCache_multiTenant() throws Exception {

    DefaultCacheHolder holder = new DefaultCacheHolder(cacheFactory, defaultOptions, defaultOptions, new MyTenantProv());

    tenantId = "ten_1";
    DefaultServerCache cache = cache(holder, Customer.class, "customer");
    assertThat(cache.getName()).isEqualTo("customer_ten_1_B");

    tenantId = "ten_2";
    DefaultServerCache cache2 = cache(holder, Customer.class, "customer");
    assertThat(cache2).isNotSameAs(cache);
    assertThat(cache2.getName()).isEqualTo("customer_ten_2_B");

    tenantId = "ten_1";
    DefaultServerCache cache3 = cache(holder, Customer.class, "customer");
    assertThat(cache3).isSameAs(cache);
  }

  @Test
  public void clearAll() throws Exception {
    DefaultCacheHolder holder = new DefaultCacheHolder(cacheFactory, defaultOptions, defaultOptions, null);
    DefaultServerCache cache = cache(holder, Customer.class, "customer");
    cache.put("foo", "foo");
    assertThat(cache.size()).isEqualTo(1);
    holder.clearAll();

    assertThat(cache.size()).isEqualTo(0);
  }

  @Test
  public void clearAll_multiTenant() throws Exception {
    DefaultCacheHolder holder = new DefaultCacheHolder(cacheFactory, defaultOptions, defaultOptions, new MyTenantProv());
    DefaultServerCache cache = cache(holder, Customer.class, "customer");
    cache.put("foo", "foo");
    assertThat(cache.size()).isEqualTo(1);

    holder.clearAll();
    assertThat(cache.size()).isEqualTo(0);
  }

  private class MyTenantProv implements CurrentTenantProvider {

    @Override
    public String currentId() {
      return tenantId;
    }
  }
}
