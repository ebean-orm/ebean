package io.ebean.config.dbplatform;

/**
 * Adds LIMIT OFFSET clauses to a SQL query.
 */
public class BasicSqlLimitOffset implements BasicSqlLimiter {

  /**
   * LIMIT keyword.
   */
  private static final String LIMIT = "limit";

  /**
   * OFFSET keyword.
   */
  private static final String OFFSET = "offset";

  @Override
  public String limit(String dbSql, int firstRow, int maxRows) {

    StringBuilder sb = new StringBuilder(50 + dbSql.length());

    sb.append(dbSql);

    if (maxRows > 0) {
      sb.append(" ").append(LIMIT);
      sb.append(" ").append(maxRows);
    }
    if (firstRow > 0) {
      sb.append(" ").append(OFFSET).append(" ");
      sb.append(firstRow);
    }
    return sb.toString();
  }

}
