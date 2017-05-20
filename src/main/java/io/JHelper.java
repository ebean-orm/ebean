package io;

public final class JHelper {

  /** Private default constructor, as this is a utility class. */
  private JHelper() { }

  /**
   * Compares 2 Objects to see if they refer to the same point in memory.
   * @param o1 Object 1.
   * @param o2 Object 2.
   * @return True if both object point to the same point in memory, false if not.
   */
  public static boolean objectSameReference(Object o1, Object o2) {
    return o1 == o2;
  }
}
