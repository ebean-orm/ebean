package io.ebeaninternal.server.cache;

import io.ebean.cache.ServerCache;
import io.ebean.cache.ServerCacheFactory;
import io.ebean.cache.ServerCacheOptions;
import io.ebean.cache.ServerCacheType;
import io.ebean.config.DatabaseConfig;
import io.ebeaninternal.server.transaction.TableModState;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Contact;
import org.tests.model.basic.Customer;

import static org.assertj.core.api.Assertions.assertThat;


class DefaultCacheHolderTest {

  private final ThreadLocal<String> tenantId = new ThreadLocal<>();

  private final ServerCacheFactory cacheFactory = new DefaultServerCacheFactory();
  private final ServerCacheOptions defaultOptions = new ServerCacheOptions();

  private CacheManagerOptions options() {
    return new CacheManagerOptions(null, new DatabaseConfig(), true)
      .with(defaultOptions, defaultOptions)
      .with(cacheFactory, new TableModState());
  }

  @Test
  void getCache_normal() {
    DefaultCacheHolder holder = new DefaultCacheHolder(options());

    ServerCache cache = cache(holder, Customer.class);
    ServerCache cache1 = cache(holder, Customer.class);
    assertThat(cache1).isSameAs(cache);

    ServerCache cache2 = cache(holder, Contact.class);
    assertThat(cache1).isNotSameAs(cache2);
  }

  private ServerCache cache(DefaultCacheHolder holder, Class<?> type) {
    return holder.getCache(type, ServerCacheType.BEAN);
  }

  @Test
  void getCache_multiTenant() throws Exception {
    CacheManagerOptions builder = options().with(tenantId::get);

    DefaultCacheHolder holder = new DefaultCacheHolder(builder);

    tenantId.set("ten_1");
    ServerCache cache = cache(holder, Customer.class);
    cache.put("1", "value-for-tenant1");
    cache.put("2", "an other value-for-tenant1");

    assertThat(cache.size()).isEqualTo(2);

    tenantId.set("ten_2");

    cache.put("1", "value-for-tenant2");
    cache.put("2", "an other value-for-tenant2");

    assertThat(cache.size()).isEqualTo(4);

    assertThat(cache.get("1")).isEqualTo("value-for-tenant2");
    assertThat(cache.get("2")).isEqualTo("an other value-for-tenant2");


    tenantId.set("ten_1");

    assertThat(cache.get("1")).isEqualTo("value-for-tenant1");
    assertThat(cache.get("2")).isEqualTo("an other value-for-tenant1");

    Exception[] exInThread = new Exception[1];
    Thread t = new Thread(() -> {
      try {
        assertThat(cache.get("1")).isNull();
        tenantId.set("ten_2");

        cache.put("1", "value-for-tenant2");
        cache.put("2", "an other value-for-tenant2");

        tenantId.set(null);

        cache.clear();
      } catch (Exception e) {
        exInThread[0] = e;
      }
    });

    // do some async work
    t.start();
    t.join();
    if (exInThread[0] != null) {
      throw exInThread[0];
    }
    assertThat(cache.size()).isEqualTo(0);
  }

  @Test
  void clearAll() {
    DefaultCacheHolder holder = new DefaultCacheHolder(options());
    ServerCache cache = cache(holder, Customer.class);
    cache.put("foo", "foo");
    assertThat(cache.size()).isEqualTo(1);
    holder.clearAll();

    assertThat(cache.size()).isEqualTo(0);
  }

  @Test
  void clearAll_multiTenant() {
    CacheManagerOptions options = options().with(tenantId::get);

    DefaultCacheHolder holder = new DefaultCacheHolder(options);
    ServerCache cache = cache(holder, Customer.class);
    cache.put("foo", "foo");
    assertThat(cache.size()).isEqualTo(1);

    holder.clearAll();
    assertThat(cache.size()).isEqualTo(0);
  }

}
