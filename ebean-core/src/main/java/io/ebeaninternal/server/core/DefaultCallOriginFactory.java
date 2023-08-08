package io.ebeaninternal.server.core;

import io.ebean.bean.CallOrigin;
import io.ebean.bean.CallStack;
import io.ebean.util.StackWalkFilter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Default CallStackFactory where the Hash function for StackTraceElement includes the line number.
 */
final class DefaultCallOriginFactory implements CallOriginFactory {

  private final int maxCallStack;

  DefaultCallOriginFactory(int maxCallStack) {
    this.maxCallStack = maxCallStack;
  }

  @Override
  public CallOrigin createCallOrigin() {
    final var frames = StackWalker.getInstance().walk(this::filter);
    if (frames.isEmpty()) {
      // this should not really happen
      throw new RuntimeException("stackFrames filtered to empty for stack: " + Arrays.toString(Thread.currentThread().getStackTrace()));
    }
    return new CallStack(frames);
  }

  private <T> List<StackWalker.StackFrame> filter(Stream<StackWalker.StackFrame> frames) {
    return frames.filter(StackWalkFilter.filter())
      .limit(maxCallStack)
      .collect(Collectors.toList());
  }

}
