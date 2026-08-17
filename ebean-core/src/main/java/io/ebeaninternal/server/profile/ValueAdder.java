package io.ebeaninternal.server.profile;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

/**
 * Accumulates a value while supporting cumulative and reset-based delta reads.
 */
final class ValueAdder {

  private final LongAdder value = new LongAdder();
  private final AtomicLong previousValue = new AtomicLong();

  void add(long amount) {
    value.add(amount);
  }

  long cumulative() {
    return value.sum();
  }

  long delta() {
    long currentValue = value.sum();
    long previous = previousValue.getAndSet(currentValue);
    return currentValue >= previous ? currentValue - previous : currentValue;
  }

  long getAndReset() {
    long currentValue = value.sumThenReset();
    previousValue.set(0);
    return currentValue;
  }

  void reset() {
    value.reset();
    previousValue.set(0);
  }

  long currentValue() {
    return value.sum();
  }
}
