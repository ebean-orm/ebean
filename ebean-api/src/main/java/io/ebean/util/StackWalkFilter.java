package io.ebean.util;

import java.util.function.Predicate;

/**
 * Provides a stack filter that excludes ebean and jdk code.
 */
public final class StackWalkFilter {

  private static final Filter FILTER = new Filter();

  /**
   * Return a stack filter that excludes ebean and jdk code.
   */
  public static Predicate<StackWalker.StackFrame> filter() {
    return FILTER;
  }

  private static class Filter implements Predicate<StackWalker.StackFrame> {

    @Override
    public boolean test(StackWalker.StackFrame stackFrame) {
      return !stackFrame.getClassName().startsWith("io.ebean")
        && !stackFrame.getClassName().startsWith("jdk.")
        && !stackFrame.getClassName().startsWith("java.")
        && !stackFrame.getMethodName().startsWith("_ebean_");
    }
  }
}
