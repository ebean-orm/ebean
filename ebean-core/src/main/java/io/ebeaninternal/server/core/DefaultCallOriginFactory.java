package io.ebeaninternal.server.core;

import io.ebean.bean.CallOrigin;
import io.ebean.bean.CallStack;

import java.util.Arrays;

/**
 * Default CallStackFactory where the Hash function for StackTraceElement includes the line number.
 */
public final class DefaultCallOriginFactory implements CallOriginFactory {

  private static final int IGNORE_LEADING_ELEMENTS = 5;

  private static final String IO_EBEAN = "io.ebean";

  private final int maxCallStack;

  DefaultCallOriginFactory(int maxCallStack) {
    this.maxCallStack = maxCallStack;
  }

  @Override
  public CallOrigin createCallOrigin() {
     StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();

    // ignore the first 6 as they are always avaje stack elements
    int startIndex = IGNORE_LEADING_ELEMENTS;

    // find the first non-avaje stackElement
    for (; startIndex < stackTrace.length; startIndex++) {
      if (!ignore(stackTrace[startIndex])) {
        break;
      }
    }

    int stackLength = stackTrace.length - startIndex;
    if (stackLength > maxCallStack) {
      // maximum of maxCallStack stackTrace elements
      stackLength = maxCallStack;
    }

    // create the 'interesting' part of the stackTrace
    StackTraceElement[] finalTrace = new StackTraceElement[stackLength];
    System.arraycopy(stackTrace, startIndex, finalTrace, 0, stackLength);

    if (stackLength < 1) {
      // this should not really happen
      throw new RuntimeException("StackTraceElement size 0?  stack: " + Arrays.toString(stackTrace));
    }

    return createCallStack(finalTrace);
  }

  private boolean ignore(StackTraceElement element) {
    if (element.getClassName().startsWith(IO_EBEAN)) {
      return true;
    }
    return element.getMethodName().startsWith("_ebean_");
  }

  private CallOrigin createCallStack(StackTraceElement[] finalTrace) {
    return new CallStack(finalTrace, finalTrace[0].hashCode(), pathHash(finalTrace));
  }

  /**
   * Return the hash code for the path excluding the first element.
   */
  private int pathHash(StackTraceElement[] callStack) {

    int hc = 0;
    for (int i = 1; i < callStack.length; i++) {
      hc = 92821 * hc + callStack[i].hashCode();
    }
    return hc;
  }

}
