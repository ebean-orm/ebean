package io.ebeaninternal.server.el;

/**
 * Case insensitive string matching.
 * <p>
 * Provides an alternative to using regular expressions.
 * </p>
 */
public final class CharMatch {

  private final char[] upperChars;

  private final int maxLength;

  public CharMatch(String s) {
    this.upperChars = s.toUpperCase().toCharArray();
    this.maxLength = upperChars.length;
  }

  public boolean startsWith(String other) {

    if (other == null || other.length() < maxLength) {
      return false;
    }

    char ta[] = other.toCharArray();

    int pos = -1;
    while (++pos < maxLength) {
      char c1 = upperChars[pos];
      char c2 = Character.toUpperCase(ta[pos]);
      if (c1 != c2) {
        return false;
      }
    }
    return true;
  }

  public boolean endsWith(String other) {

    if (other == null || other.length() < maxLength) {
      return false;
    }

    char ta[] = other.toCharArray();

    int offset = ta.length - maxLength;
    int pos = maxLength;
    while (--pos >= 0) {
      char c1 = upperChars[pos];
      char c2 = Character.toUpperCase(ta[offset + pos]);
      if (c1 != c2) {
        return false;
      }
    }
    return true;
  }

  public boolean contains(String other) {

    if (other == null || other.length() < maxLength) {
      return false;
    }
    if (maxLength == 0) {
      return true;
    }

    char ta[] = other.toCharArray();

    int otherLength = ta.length;
    for (int i = 0; i < otherLength; i++) {

      char ch = Character.toUpperCase(ta[i]);

      /* Look for first character. */
      if (ch != upperChars[0]) {
        while (++i < otherLength) {
          ch = Character.toUpperCase(ta[i]);
          if (ch == upperChars[0]) {
            break;
          }
        }
      }

      /* Found first character, now look at the rest of ta */
      if (i < otherLength) {
          int j = i + 1;
          int end = j + maxLength - 1;
          for (int k = 1; j < end && Character.toUpperCase(ta[j]) == upperChars[k]; j++, k++);

          if (j == end) {
              return true;
          }
      }
    }
    return false;
  }
}
