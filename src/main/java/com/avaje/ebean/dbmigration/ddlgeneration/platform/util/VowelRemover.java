package com.avaje.ebean.dbmigration.ddlgeneration.platform.util;

/**
 * Utility to remove vowels (from constraint names primarily for Oracle and DB2).
 */
public class VowelRemover {

  /**
   * Trim a word by removing vowels skipping some initial characters.
   */
  public static String trim(String word, int skipChars) {

    if (word.length() < skipChars) {
      return word;
    }

    StringBuilder res = new StringBuilder();
    res.append(word.substring(0, skipChars));

    for (int i = skipChars; i < word.length(); i++) {
      char ch = word.charAt(i);
      if (!isVowel(ch)) {
        res.append(ch);
      }
    }
    return res.toString();
  }

  private static boolean isVowel(char ch) {
    ch = Character.toLowerCase(ch);
    return ch == 'a' || ch == 'e' || ch == 'i' || ch == 'o' || ch == 'u';
  }
}
