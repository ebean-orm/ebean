package io.ebean.redis;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import redis.clients.jedis.Jedis;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Auto-detected JUnit5 extension (see {@code junit-platform.properties}) that flushes the shared
 * Redis test container exactly once per JVM, before the first test class in this module runs.
 *
 * <p>The Redis test container is intentionally long-lived and reused across test runs - and even
 * shared with the sibling ebean-redisson module (same fixed-name/fixed-port container) - to avoid
 * restart cost, especially with parallel reactor builds. It is never flushed between runs. Without
 * this, stale keys/counters left over from an earlier run can leak into hit/miss/TTL assertions
 * that assume a cold cache, causing hard-to-reproduce flakiness.
 */
public class RedisFlushExtension implements BeforeAllCallback {

  private static final AtomicBoolean FLUSHED = new AtomicBoolean();

  @Override
  public void beforeAll(ExtensionContext context) {
    if (FLUSHED.compareAndSet(false, true)) {
      flush();
    }
  }

  private static void flush() {
    try (Jedis jedis = new Jedis("localhost", 6379)) {
      jedis.flushDB();
    } catch (Exception e) {
      // best effort - if Redis isn't reachable here, individual tests skip via their own checks
    }
  }
}
