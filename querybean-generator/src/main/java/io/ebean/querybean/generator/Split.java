package io.ebean.querybean.generator;

/**
 * Helper for splitting package and class name.
 */
class Split {

  /**
   * Split into package and class name.
   */
  static String[] split(String className) {
    String[] result = new String[2];
    int startPos = className.lastIndexOf('.');
    if (startPos == -1) {
      result[1] = className;
      return result;
    }
    result[0] = className.substring(0, startPos);
    result[1] = className.substring(startPos + 1);
    return result;
  }

  /**
   * Trim off package to return the simple class name.
   */
  static String shortName(String className) {
    int startPos = className.lastIndexOf('.');
    if (startPos == -1) {
      return className;
    }
    return className.substring(startPos + 1);
  }

}
