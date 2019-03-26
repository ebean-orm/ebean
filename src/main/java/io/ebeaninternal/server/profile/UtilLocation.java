package io.ebeaninternal.server.profile;

final class UtilLocation {

  static String label(String shortDescription) {
    int pos = shortDescription.indexOf("(");
    if (pos == -1) {
      return shortDescription;
    } else {
      return shortDescription.substring(0, pos);
    }
  }

}
