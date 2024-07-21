package io.ebean.querybean.generator;

class Util {

  static String stripForMethod(String dbName) {
    return dbName.replace('-', '_');
  }

  static String packageOf(boolean nested, String originName) {
    return nested ? nestedPackageOf(originName) : packageOf(originName);
  }

  private static String nestedPackageOf(String cls) {
    int pos = cls.lastIndexOf('.');
    if (pos < 0) {
      return "";
    }
    pos = cls.lastIndexOf('.', pos - 1);
    return (pos == -1) ? "" : cls.substring(0, pos);
  }

  private static String packageOf(String cls) {
    int pos = cls.lastIndexOf('.');
    return (pos == -1) ? "" : cls.substring(0, pos);
  }

  static String shortName(boolean nested, String fullType) {
    return nested ? nestedShortName(fullType): shortName(fullType);
  }

  private static String nestedShortName(String fullType) {
    int pos = fullType.lastIndexOf('.');
    if (pos < 0) {
      return fullType;
    } else {
      pos = fullType.lastIndexOf('.', pos - 1);
      return pos < 0 ? fullType : fullType.substring(pos + 1);
    }
  }

  private static String shortName(String fullType) {
    int p = fullType.lastIndexOf('.');
    if (p == -1) {
      return fullType;
    } else {
      return fullType.substring(p + 1);
    }
  }

}
