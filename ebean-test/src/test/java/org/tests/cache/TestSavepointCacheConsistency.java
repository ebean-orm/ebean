package org.tests.cache;

import io.ebean.DB;
import io.ebean.Transaction;
import io.ebean.cache.ServerCache;
import io.ebean.cache.ServerCacheStatistics;
import io.ebean.test.LoggedSql;
import io.ebean.xtest.BaseTestCase;
import io.ebean.xtest.IgnorePlatform;
import io.ebean.annotation.Platform;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.OCachedBean;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests that savepoint (nested) transaction commits do not prematurely apply
 * cache changes before the parent transaction commits.
 * <p>
 * Before the fix, SavepointTransaction.commit() called manager.notifyOfCommit()
 * immediately, which would invalidate/update the L2 cache even if the parent
 * transaction later rolled back — leaving the cache in a stale/polluted state.
 */
public class TestSavepointCacheConsistency extends BaseTestCase {

  private final ServerCache beanCache = DB.getDefault().cacheManager().beanCache(OCachedBean.class);

  @BeforeEach
  void clearCache() {
    beanCache.clear();
  }

  /**
   * The bug scenario: savepoint commits, parent rolls back.
   * Cache must remain valid (not evicted) because the DB change was rolled back.
   * Before the fix, the premature notifyOfCommit on savepoint commit would
   * evict the cache entry, causing an unnecessary (and potentially wrong) cache miss.
   */
  @IgnorePlatform({Platform.MYSQL, Platform.SQLSERVER, Platform.HANA, Platform.ORACLE})
  @Test
  void savepointCommit_parentRollback_cacheNotPolluted() {
    OCachedBean bean = new OCachedBean();
    bean.setName("original");
    DB.save(bean);

    // Warm the cache with the original value
    DB.find(OCachedBean.class, bean.getId());

    beanCache.statistics(true); // reset hit/miss counters

    try (Transaction outer = DB.beginTransaction()) {
      outer.setNestedUseSavepoint();

      try (Transaction nested = DB.beginTransaction()) {
        bean.setName("rolled-back-change");
        DB.save(bean);
        nested.commit(); // savepoint released — must NOT flush cache yet
      }

      outer.rollback(); // DB change undone; cache must stay untouched
    }

    // Cache must still hold the original entry — no SQL should be needed
    LoggedSql.start();
    OCachedBean found = DB.find(OCachedBean.class, bean.getId());
    List<String> sql = LoggedSql.stop();

    assertThat(found.getName()).isEqualTo("original");
    assertThat(sql).as("cache should still be valid after parent rollback — no DB query expected").isEmpty();

    ServerCacheStatistics stats = beanCache.statistics(true);
    assertThat(stats.getHitCount()).as("bean should be served from cache").isEqualTo(1);
    assertThat(stats.getMissCount()).isZero();

    DB.delete(OCachedBean.class, bean.getId());
  }

  /**
   * Happy path: savepoint commits, parent commits.
   * Cache must reflect the committed change (invalidated or updated).
   * The fix must not suppress cache updates for the successful case.
   */
  @IgnorePlatform({Platform.MYSQL, Platform.SQLSERVER, Platform.HANA, Platform.ORACLE})
  @Test
  void savepointCommit_parentCommit_cacheProperlyInvalidated() {
    OCachedBean bean = new OCachedBean();
    bean.setName("before");
    DB.save(bean);

    // Warm the cache
    DB.find(OCachedBean.class, bean.getId());

    try (Transaction outer = DB.beginTransaction()) {
      outer.setNestedUseSavepoint();

      try (Transaction nested = DB.beginTransaction()) {
        bean.setName("after");
        DB.save(bean);
        nested.commit();
      }

      outer.commit(); // parent commits — cache changes should now be applied
    }

    // The bean must reflect the committed update
    OCachedBean found = DB.find(OCachedBean.class, bean.getId());
    assertThat(found.getName()).isEqualTo("after");

    DB.delete(OCachedBean.class, bean.getId());
  }

  /**
   * Multiple savepoints in the same parent: only the changes whose savepoints
   * committed are reflected when the parent commits.
   */
  @IgnorePlatform({Platform.MYSQL, Platform.SQLSERVER, Platform.HANA, Platform.ORACLE})
  @Test
  void multipleSavepoints_mixedCommitRollback_parentCommit_onlyCommittedChangesVisible() {
    OCachedBean bean = new OCachedBean();
    bean.setName("start");
    DB.save(bean);

    DB.find(OCachedBean.class, bean.getId()); // warm cache

    try (Transaction outer = DB.beginTransaction()) {
      outer.setNestedUseSavepoint();

      // First nested: commits
      try (Transaction nested1 = DB.beginTransaction()) {
        bean.setName("after-nested1");
        DB.save(bean);
        nested1.commit();
      }

      // Second nested: rolls back — its name change must not persist
      try (Transaction nested2 = DB.beginTransaction()) {
        bean.setName("after-nested2-rolled-back");
        DB.save(bean);
        nested2.rollback();
      }

      outer.commit();
    }

    OCachedBean found = DB.find(OCachedBean.class, bean.getId());
    assertThat(found.getName()).isEqualTo("after-nested1");

    DB.delete(OCachedBean.class, bean.getId());
  }

  /**
   * Savepoint commits then parent also rolls back — same cache-pollution guard,
   * but verified via LoggedSql rather than statistics, to confirm the cache
   * entry was never evicted.
   */
  @IgnorePlatform({Platform.MYSQL, Platform.SQLSERVER, Platform.HANA, Platform.ORACLE})
  @Test
  void savepointCommit_parentRollback_cacheServedWithoutDbRoundtrip() {
    OCachedBean bean = new OCachedBean();
    bean.setName("pristine");
    DB.save(bean);

    // Warm the cache
    DB.find(OCachedBean.class, bean.getId());

    try (Transaction outer = DB.beginTransaction()) {
      outer.setNestedUseSavepoint();

      try (Transaction nested = DB.beginTransaction()) {
        bean.setName("discarded");
        DB.save(bean);
        nested.commit();
      }

      outer.rollback();
    }

    LoggedSql.start();
    OCachedBean result = DB.find(OCachedBean.class, bean.getId());
    List<String> sql = LoggedSql.stop();

    assertThat(result.getName()).isEqualTo("pristine");
    assertThat(sql).as("no SQL expected — cache entry must survive parent rollback").isEmpty();

    DB.delete(OCachedBean.class, bean.getId());
  }
}
