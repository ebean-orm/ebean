package io.ebeaninternal.server.persist;

/**
 * Utility to improve logging of raw SQL that contains new line characters.
 */
public class TrimLogSql {

  /**
   * Replace new line chars for nicer logging of multi-line sql strings.
   */
  public static String trim(String sql) {
    return sql.replace("\n","\\n ");
  }
}
