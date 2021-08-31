package io.ebeaninternal.server.core;

import java.util.HashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Used to reduce memory consumption of strings used in deployment processing.
 * <p>
 * Using this for now instead of String.intern() to avoid any unexpected
 * increase in PermGen space.
 */
public final class InternString {

  private static final HashMap<String, String> map = new HashMap<>();

  private static final ReentrantLock lock = new ReentrantLock();

  /**
   * Return the shared instance of this string.
   */
  public static String intern(String s) {
    if (s == null) {
      return null;
    }
    lock.lock();
    try {
      String v = map.get(s);
      if (v != null) {
        return v;
      } else {
        map.put(s, s);
        return s;
      }
    } finally {
      lock.unlock();
    }
  }
}
