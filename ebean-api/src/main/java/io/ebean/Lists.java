package io.ebean;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper methods for Lists.
 */
public interface Lists {

  /**
   * Partition the source List into sub-lists with a maximum size.
   *
   * @param max    The max size of each partition
   * @param source The source list
   * @param <T>    The list element type
   * @return List of partitions
   */
  static <T> List<List<T>> partition(int max, List<T> source) {
    final int totalCount = source.size();
    if (totalCount <= max) {
      return List.of(source);
    }
    final int numOfPartitions = (totalCount + max - 1) / max;  // round up
    final var dest = new ArrayList<List<T>>(numOfPartitions);
    for (int i = 0; i < numOfPartitions; i++) {
      final int from = i * max;
      final int to = Math.min(from + max, totalCount);
      dest.add(source.subList(from, to));
    }
    return dest;
  }
}
