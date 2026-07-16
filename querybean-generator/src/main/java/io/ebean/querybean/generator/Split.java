package io.ebean.querybean.generator;

import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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

  /**
   * Returns the first pair of distinct full names in {@code fullNames} that share the same
   * simple (unqualified) class name, or {@code null} if there's no such collision - used to fail
   * fast, with a clear diagnostic, on an ambiguous set of imports (two different types sharing a
   * simple name) rather than silently generating source with a duplicate-import/undefined-symbol
   * compile error that's much harder to trace back to its cause.
   */
  static String[] findSimpleNameCollision(Set<String> fullNames) {
    Map<String, String> seenBySimpleName = new HashMap<>();
    for (String fullName : fullNames) {
      String existing = seenBySimpleName.putIfAbsent(shortName(fullName), fullName);
      if (existing != null && !existing.equals(fullName)) {
        return new String[] {existing, fullName};
      }
    }
    return null;
  }

}
