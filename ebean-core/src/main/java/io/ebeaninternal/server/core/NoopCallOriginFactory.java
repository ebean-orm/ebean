package io.ebeaninternal.server.core;

import io.ebean.bean.CallOrigin;
import io.ebean.bean.CallStack;

/**
 * A CallOriginFactory we can use when we don't use AutoTune.
 */
final class NoopCallOriginFactory implements CallOriginFactory {

  private static final StackTraceElement E0 = new StackTraceElement("none", "none", "none", 0);

  private final CallOrigin COMMON = new CallStack(new Object[]{E0}, 0, 0);

  @Override
  public CallOrigin createCallOrigin() {
    return COMMON;
  }
}
