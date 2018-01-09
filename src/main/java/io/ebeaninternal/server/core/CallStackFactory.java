package io.ebeaninternal.server.core;

import io.ebean.bean.CallStack;

/**
 * Creates CallStack based on the stack trace.
 */
public interface CallStackFactory {

  /**
   * Create and return the CallStack given the stack trace elements.
   */
  CallStack createCallStack();
}
