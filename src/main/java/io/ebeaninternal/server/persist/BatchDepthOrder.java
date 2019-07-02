package io.ebeaninternal.server.persist;

import java.util.HashMap;
import java.util.Map;

/**
 * Helper to determine batch execution order for BatchedBeanHolders.
 */
class BatchDepthOrder {

  private final Map<Integer, Counter> map = new HashMap<>();

  /**
   * Return the batch order for the given depth.
   */
  int orderingFor(int depth) {
    final Counter slot = map.computeIfAbsent(depth, integer -> new Counter());
    return (depth * 100) + slot.increment();
  }

  void clear() {
    map.clear();
  }

  private static class Counter {

    int count;

    int increment() {
      return count++;
    }
  }
}
