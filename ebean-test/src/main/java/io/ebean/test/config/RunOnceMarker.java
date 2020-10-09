package io.ebean.test.config;

import io.ebeaninternal.api.DbOffline;

class RunOnceMarker {

  private static boolean hasRun;

  static synchronized boolean isRun() {
    if (DbOffline.isSet() || hasRun) {
      return false;
    }
    hasRun = true;
    return true;
  }
}
