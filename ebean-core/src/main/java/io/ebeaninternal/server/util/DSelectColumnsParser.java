package io.ebeaninternal.server.util;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Splits a select clause into 'logical columns' taking into account functions and quotes.
 */
public final class DSelectColumnsParser {

  private final int end;
  private final String selectClause;
  private int pos;

  public static Set<String> parse(String sqlSelect) {
    return new DSelectColumnsParser(sqlSelect).parse();
  }

  private DSelectColumnsParser(String selectClause) {
    this.selectClause = selectClause;
    this.end = selectClause.length();
  }

  private Set<String> parse() {
    LinkedHashSet<String> columns = new LinkedHashSet<>();
    while (pos <= end) {
      columns.add(nextColumnInfo());
    }
    return columns;
  }

  private String nextColumnInfo() {
    int start = pos;
    nextComma();
    return selectClause.substring(start, pos++).trim();
  }

  private void nextComma() {
    boolean inQuote = false;
    int inBrackets = 0;
    while (pos < end) {
      char c = selectClause.charAt(pos);
      if (c == '\'') inQuote = !inQuote;
      else if (c == '(') inBrackets++;
      else if (c == ')') inBrackets--;
      else if (!inQuote && inBrackets == 0 && c == ',') {
        return;
      }
      pos++;
    }
  }
}
