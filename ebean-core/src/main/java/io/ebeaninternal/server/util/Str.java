package io.ebeaninternal.server.util;

/**
 * String utility for adding strings together.
 * <p>
 * Predicts a decent buffer size to append the strings into.
 */
public final class Str {

  /**
   * Append strings together.
   */
  public static String add(String s0, String s1, String... args) {
    // determine a decent buffer size
    int len = 16 + s0.length();
    if (s1 != null) {
      len += s1.length();
    }
    for (String arg1 : args) {
      len += (arg1 == null) ? 0 : arg1.length();
    }

    // append all the strings into the buffer
    StringBuilder sb = new StringBuilder(len);
    sb.append(s0);
    if (s1 != null) {
      sb.append(s1);
    }
    for (String arg : args) {
      if (arg != null) {
        sb.append(arg);
      }
    }
    return sb.toString();
  }

  /**
   * Append two strings together.
   */
  public static String add(String s0, String s1) {
    //noinspection StringBufferReplaceableByString
    StringBuilder sb = new StringBuilder(s0.length() + s1.length() + 5);
    return sb.append(s0).append(s1).toString();
  }

}
