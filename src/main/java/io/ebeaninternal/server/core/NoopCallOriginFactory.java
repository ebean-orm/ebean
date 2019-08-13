package io.ebeaninternal.server.core;

import io.ebean.bean.CallOrigin;
import io.ebean.bean.CallStack;

/**
 * A CallOriginFactory we can use when we don't use AutoTune.
 */
class NoopCallOriginFactory implements CallOriginFactory {

  private final CallOrigin COMMON = new CallStack(Thread.currentThread().getStackTrace(), 0, 0);

  @Override
  public CallOrigin createCallOrigin() {
    return COMMON;
  }
}
