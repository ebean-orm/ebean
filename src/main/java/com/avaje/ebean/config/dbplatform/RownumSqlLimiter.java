package com.avaje.ebean.config.dbplatform;

/**
 * Add ROWNUM column etc around SQL query to limit results.
 */
public class RownumSqlLimiter implements SqlLimiter {

  private final String rnum;

  private final boolean useFirstRowsHint;

  /**
   * Create with default inner rownum column alias and used FIRST_ROWS hint.
   */
  public RownumSqlLimiter() {
    this("rn_", true);
  }

  /**
   * Specify the inner rownum column alias and whether to include the FIRST_ROWS
   * hint.
   */
  public RownumSqlLimiter(String rnum, boolean useFirstRowsHint) {
    this.rnum = rnum;
    this.useFirstRowsHint = useFirstRowsHint;
  }

  public SqlLimitResponse limit(SqlLimitRequest request) {

    // select *
    // from ( select /*+ FIRST_ROWS(n) */ ROWNUM rnum, a.*
    // from ( your_query_goes_here,
    // with order by ) a
    // where ROWNUM <=
    // :MAX_ROW_TO_FETCH )
    // where rnum >= :MIN_ROW_TO_FETCH;

    String dbSql = request.getDbSql();
    
    StringBuilder sb = new StringBuilder(60 + dbSql.length());

    int firstRow = request.getFirstRow();

    int lastRow = request.getMaxRows();
    if (lastRow > 0) {
      lastRow = lastRow + firstRow;
    }

    sb.append("select * from ( ");

    sb.append("select ");
    if (useFirstRowsHint && request.getMaxRows() > 0) {
      sb.append("/*+ FIRST_ROWS(").append(request.getMaxRows()).append(") */ ");
    }

    sb.append("rownum ").append(rnum).append(", a.* ");
    sb.append(" from (");

    sb.append(" select ");
    if (request.isDistinct()) {
      sb.append("distinct ");
    }
    sb.append(dbSql);

    sb.append(NEW_LINE).append("  ) a ");
    if (lastRow > 0) {
      sb.append(" where rownum <= ").append(lastRow);
    }
    sb.append(" ) ");
    if (firstRow > 0) {
      sb.append(" where ");
      sb.append(rnum).append(" > ").append(firstRow);
    }

    String sql = request.getDbPlatform().completeSql(sb.toString(), request.getOrmQuery());

    return new SqlLimitResponse(sql, true);
  }

}
