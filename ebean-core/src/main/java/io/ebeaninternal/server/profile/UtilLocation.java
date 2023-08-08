package io.ebeaninternal.server.profile;

final class UtilLocation {

  static String loc(String full) {
    return loc(full, false);
  }

  static String loc(String full, boolean includeLineNumber) {
    final int pos = full.lastIndexOf('(');
    if (pos > -1) {
      return !includeLineNumber ? full.substring(0, pos) : full.substring(0, pos) + lineNumberFrom(full);
    } else {
      return full;
    }
  }

  private static String lineNumberFrom(String full) {
    int pos = full.lastIndexOf(':');
    return pos == -1 ? "" : full.substring(pos, full.length() - 1);
  }

  static String label(String location) {
    return trimInit(shortDesc(location));
  }

  private static String shortDesc(String location) {
    int pos = location.lastIndexOf('.');
    if (pos > -1) {
      pos = location.lastIndexOf('.', pos - 1);
      if (pos > -1) {
        return location.substring(pos + 1);
      }
    }
    return location;
  }

  /**
   * Trim constructor init to be "safe" without greater than or less than chars.
   */
  private static String trimInit(String desc) {
    if (desc.endsWith("<init>")) {
      // trim to make a safe label
      return desc.substring(0, desc.length() - 6) + "init";
    }
    return desc;
  }
}
