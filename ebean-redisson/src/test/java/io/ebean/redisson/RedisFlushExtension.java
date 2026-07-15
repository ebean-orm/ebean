package io.ebean.redisson;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Auto-detected JUnit5 extension (see {@code junit-platform.properties}) that flushes the shared
 * Redis test container exactly once per JVM, before the first test class in this module runs.
 *
 * <p>The Redis test container is intentionally long-lived and reused across test runs - and even
 * shared with the sibling ebean-redis module (same fixed-name/fixed-port container) - to avoid
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
    try {
      RedissonClient client = Redisson.create(loadConfig());
      try {
        client.getKeys().flushdb();
      } finally {
        client.shutdown();
      }
    } catch (Exception e) {
      // best effort - if Redis isn't reachable here, individual tests skip via their own checks
    }
  }

  private static Config loadConfig() {
    InputStream is = RedisFlushExtension.class.getClassLoader().getResourceAsStream("redisson-config.yaml");
    if (is != null) {
      return Config.fromYAML(is);
    }
    Config cfg = new Config();
    cfg.useSingleServer().setAddress("redis://localhost:6379");
    return cfg;
  }
}
