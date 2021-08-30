package io.ebeaninternal.server.executor;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ThreadFactory for Daemon threads.
 * <p>
 * Daemon threads do not stop a JVM stopping. If an application only has Daemon
 * threads left it will shutdown.
 * <p>
 * In using Daemon threads you need to either not care about being interrupted
 * on shutdown or register with the JVM shutdown hook to perform a nice shutdown
 * of the daemon threads etc.
 */
public final class DaemonThreadFactory implements ThreadFactory {

  private final AtomicInteger threadNumber = new AtomicInteger(1);
  private final String namePrefix;

  public DaemonThreadFactory(String namePrefix) {
    this.namePrefix = namePrefix;
  }

  @Override
  public Thread newThread(Runnable r) {
    Thread t = new Thread(null, r, namePrefix + threadNumber.getAndIncrement(), 0);
    t.setDaemon(true);
    if (t.getPriority() != Thread.NORM_PRIORITY) {
      t.setPriority(Thread.NORM_PRIORITY);
    }
    return t;
  }
}
