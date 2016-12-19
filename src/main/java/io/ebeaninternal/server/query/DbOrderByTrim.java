package io.ebeaninternal.server.query;

import java.util.regex.Pattern;

class DbOrderByTrim {

  private static final Pattern orderByTrim = Pattern.compile("(?i)\\b asc\\b|\\b desc\\b");

  /**
   * Convert the dbOrderBy clause to be safe for adding to select or distinct on.
   */
  static String trim(String dbOrderBy) {
    // just remove the ASC and DESC keywords
    return orderByTrim.matcher(dbOrderBy).replaceAll("");
  }

}
