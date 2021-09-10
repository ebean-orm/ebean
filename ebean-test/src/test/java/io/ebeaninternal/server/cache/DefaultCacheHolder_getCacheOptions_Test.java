package io.ebeaninternal.server.cache;

import io.ebean.cache.ServerCacheOptions;
import io.ebean.cache.ServerCacheType;
import io.ebean.config.DatabaseConfig;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Article;
import org.tests.model.basic.Order;
import org.tests.model.basic.Product;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DefaultCacheHolder_getCacheOptions_Test {

  private final DefaultCacheHolder cacheHolder;

  public DefaultCacheHolder_getCacheOptions_Test() {

    ServerCacheOptions defaultOptions = new ServerCacheOptions();
    defaultOptions.setMaxSize(10000);
    defaultOptions.setMaxSecsToLive(120);

    CacheManagerOptions builder = new CacheManagerOptions(null, new DatabaseConfig(), true)
      .with(defaultOptions, defaultOptions);

    this.cacheHolder = new DefaultCacheHolder(builder);
  }

  @Test
  public void beanOptions_when_set() {

    ServerCacheOptions options = cacheHolder.getCacheOptions(Article.class, ServerCacheType.BEAN);
    assertEquals(options.getMaxSecsToLive(), 45);
  }

  @Test
  public void beanOptions_when_notSet_expect_default() {

    ServerCacheOptions options = cacheHolder.getCacheOptions(Order.class, ServerCacheType.BEAN);
    assertEquals(options.getMaxSecsToLive(), 120);
  }

  @Test
  public void queryOptions_when_set() {

    ServerCacheOptions options = cacheHolder.getCacheOptions(Product.class, ServerCacheType.QUERY);
    assertEquals(options.getMaxSecsToLive(), 15);
  }

  @Test
  public void queryOptions_when_notSet_expect_default() {

    ServerCacheOptions options = cacheHolder.getCacheOptions(Order.class, ServerCacheType.QUERY);
    assertEquals(options.getMaxSecsToLive(), 120);
  }
}
