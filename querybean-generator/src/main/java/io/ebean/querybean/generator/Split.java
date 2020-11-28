package io.ebean.querybean.generator;

import java.util.AbstractMap.SimpleEntry;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;

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

  static Entry<String, Set<String>> genericsSplit(String signature) {
    StringBuilder simpleSignature = new StringBuilder();
    final StringTokenizer tokenizer = new StringTokenizer(signature, ",<> ", true);

    Set<String> assocImports = new HashSet<>();
    while (tokenizer.hasMoreTokens()) {
      final String token = tokenizer.nextToken();
      if (token.length() == 1 && ",<> ".indexOf(token.charAt(0)) >= 0) {
        simpleSignature.append(token);
      } else {
        simpleSignature.append(Split.shortName(token));
        assocImports.add(token);
      }
    }
    return new SimpleEntry<>(simpleSignature.toString(), assocImports);
  }

}
