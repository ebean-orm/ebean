package io.ebeaninternal.util;

import io.ebeaninternal.util.SortByClause.Property;

public final class SortByClauseParser {

  private final String rawSortBy;

  public static SortByClause parse(String rawSortByClause) {
    return new SortByClauseParser(rawSortByClause).parse();
  }

  private SortByClauseParser(String rawSortByClause) {
    this.rawSortBy = rawSortByClause.trim();
  }

  private SortByClause parse() {

    SortByClause sortBy = new SortByClause();

    String[] sections = rawSortBy.split(",");
    for (String section : sections) {
      Property p = parseSection(section.trim());
      if (p == null) {
        break;
      } else {
        sortBy.add(p);
      }

    }

    return sortBy;
  }

  private Property parseSection(String section) {
    if (section.isEmpty()) {
      return null;
    }
    String[] words = section.split(" ");
    if (words.length < 1 || words.length > 3) {
      throw new RuntimeException("Expecting 1 to 3 words in [" + section + "] but got " + words.length);
    }

    Boolean nullsHigh = null;
    boolean ascending = true;
    String propName = words[0];
    if (words.length > 1) {
      if (words[1].startsWith("nulls")) {
        nullsHigh = isNullsHigh(words[1]);

      } else {
        ascending = isAscending(words[1]);
      }
    }
    if (words.length > 2) {
      if (words[2].startsWith("nulls")) {
        nullsHigh = isNullsHigh(words[2]);

      } else {
        ascending = isAscending(words[2]);
      }
    }

    return new Property(propName, ascending, nullsHigh);
  }

  private Boolean isNullsHigh(String word) {
    if (SortByClause.NULLSHIGH.equalsIgnoreCase(word)) {
      return Boolean.TRUE;
    }
    if (SortByClause.NULLSLOW.equalsIgnoreCase(word)) {
      return Boolean.FALSE;
    }
    throw new RuntimeException("Expecting nullsHigh or nullsLow but got [" + word + "] in " + rawSortBy);
  }


  private boolean isAscending(String word) {
    if (SortByClause.ASC.equalsIgnoreCase(word)) {
      return true;
    }
    if (SortByClause.DESC.equalsIgnoreCase(word)) {
      return false;
    }
    throw new RuntimeException("Expect ASC or DESC but got " + word + " in " + rawSortBy);
  }

}
