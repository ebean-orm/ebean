package io.ebeaninternal.server.query;

import java.util.regex.Pattern;

final class DbOrderByTrim {

  private static final Pattern orderByTrim = Pattern.compile("(?i) asc\\b| desc\\b| nulls first\\b| nulls last\\b");

  /**
   * Convert the dbOrderBy clause to be safe for adding to select or distinct on.
   */
  static String trim(String dbOrderBy) {
    // just remove the ASC and DESC keywords
    return orderByTrim.matcher(dbOrderBy).replaceAll("");
  }

  /**
   * Checks, if <code>sql</code> contains the <code>column</code>.
   * <p>
   * SQL is normally comma separated: "t0.id, t1.id, t2.name"
   */
  static boolean contains(String sql, String column) {
    if (sql.endsWith(column)) { // simplest way. sql ends with the column
      return true;
    } else if (sql.contains(column)) {
      // We need to check, if it is really the correct column,
      // sql="t0.name_short, t0.name_long" will match on column="t0.name"
      return sql.contains(column + ",");
    } else {
      return false;
    }
  }

}
