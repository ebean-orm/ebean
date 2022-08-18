package io.ebeaninternal.api;

import io.avaje.applog.AppLog;

/**
 * Common loggers used in ebean-core.
 */
public final class CoreLog {

  public static final System.Logger log = AppLog.getLogger("io.ebean.core");
  public static final System.Logger internal = AppLog.getLogger("io.ebean.internal");
}
