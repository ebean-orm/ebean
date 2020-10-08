package io.ebeaninternal.server.profile;

final class UtilLocation {

  static String label(String shortDescription) {
    int pos = shortDescription.indexOf("(");
    if (pos == -1) {
      return shortDescription;
    } else {
      return trimInit(shortDescription.substring(0, pos));
    }
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
