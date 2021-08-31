package io.ebean.test.config;

import io.ebeaninternal.api.DbOffline;

import java.util.concurrent.locks.ReentrantLock;

class RunOnceMarker {

  private static final ReentrantLock lock = new ReentrantLock();
  private static boolean hasRun;

  static boolean isRun() {
    lock.lock();
    try {
      if (DbOffline.isSet() || hasRun) {
        return false;
      }
      hasRun = true;
      return true;
    } finally {
      lock.unlock();
    }
  }
}
