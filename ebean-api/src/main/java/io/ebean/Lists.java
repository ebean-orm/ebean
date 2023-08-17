package io.ebean;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper methods for Lists.
 */
public final class Lists {

  private Lists() {
  }

  /**
   * Partition the source List into sub-lists with a maximum size.
   * <p>
   * The sub-lists will all be the max size except for the last sub-list
   * which can potentially be smaller.
   *
   * @param source The source list
   * @param max    The max size of each partition
   * @param <T>    The list element type
   * @return List of sub-list partitions
   */
  public static <T> List<List<T>> partition(List<T> source, int max) {
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
