package org.tests.model.basic.cache;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.cache.ServerCache;
import io.ebean.cache.ServerCacheStatistics;
import io.ebeantest.LoggedSql;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestCacheWithSoftDelete extends BaseTestCase {

  private final ServerCache beanCache = DB.cacheManager().beanCache(ESoftWithCache.class);

  @Test
  public void idIn_expect_hitCache() {
    ESoftWithCache bean = new ESoftWithCache("hello");
    DB.save(bean);

    final List<ESoftWithCache> found = DB.find(ESoftWithCache.class)
      .where().idIn(bean.id())
      .findList();
    assertThat(found).hasSize(1);

    // try to hit the cache
    assertThat(stats().getPutCount()).isEqualTo(1);

    LoggedSql.start();
    // cache hit success this time
    final List<ESoftWithCache> foundAgain = DB.find(ESoftWithCache.class)
      .where().idIn(bean.id())
      .findList();

    final List<String> sql = LoggedSql.stop();
    assertThat(sql).isEmpty();
    assertThat(foundAgain).hasSize(1);
    assertThat(stats().getHitCount()).isEqualTo(1);
  }

  private ServerCacheStatistics stats() {
    return beanCache.statistics(true);
  }
}
