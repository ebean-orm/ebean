package io.ebeaninternal.server.core;

import java.util.HashMap;

/**
 * Used to reduce memory consumption of strings used in deployment processing.
 * <p>
 * Using this for now instead of String.intern() to avoid any unexpected
 * increase in PermGen space.
 * </p>
 */
public final class InternString {

  private static final HashMap<String, String> map = new HashMap<>();


  /**
   * Return the shared instance of this string.
   */
  public static String intern(String s) {

    if (s == null) {
      return null;
    }

    //noinspection SynchronizationOnStaticField
    synchronized (map) {
      String v = map.get(s);
      if (v != null) {
        return v;
      } else {
        map.put(s, s);
        return s;
      }

    }
  }
}
