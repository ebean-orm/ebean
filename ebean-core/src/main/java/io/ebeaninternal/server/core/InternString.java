package io.ebeaninternal.server.core;

/**
 * Used to reduce memory consumption of strings used in deployment processing.
 */
public final class InternString {

  /**
   * Return the shared instance of this string.
   */
  public static String intern(String s) {
    return s == null ? null : s.intern();
  }
}
