package io.ebeaninternal.server.core;

import io.ebean.bean.CallStack;

/**
 * A CallStackFactory we can use when we don't use AutoTune.
 */
class NoopCallStackFactory implements CallStackFactory {

  private final CallStack COMMON = new CallStack(Thread.currentThread().getStackTrace(), 0, 0);

  @Override
  public CallStack createCallStack() {
    return COMMON;
  }
}
