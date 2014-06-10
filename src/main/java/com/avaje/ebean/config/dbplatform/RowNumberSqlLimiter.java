package com.avaje.ebean.config.dbplatform;

/**
 * Adds the ROW_NUMBER() OVER function to a query.
 */
public class RowNumberSqlLimiter implements SqlLimiter {

  /**
   * ROW_NUMBER() OVER (ORDER BY
   */
  private static final String ROW_NUMBER_OVER = "row_number() over (order by ";

  /**
   * ) as rn,
   */
  private static final String ROW_NUMBER_AS = ") as rn, ";

  private final String rowNumberWindowAlias;

  /**
   * Specify the name of the rowNumberWindowAlias.
   */
  public RowNumberSqlLimiter(String rowNumberWindowAlias) {
    this.rowNumberWindowAlias = rowNumberWindowAlias;
  }

  public RowNumberSqlLimiter() {
    this("as limitresult");
  }

  public SqlLimitResponse limit(SqlLimitRequest request) {

    String dbSql = request.getDbSql();
    
    StringBuilder sb = new StringBuilder(60 + dbSql.length());

    int firstRow = request.getFirstRow();

    int lastRow = request.getMaxRows();
    if (lastRow > 0) {
      lastRow = lastRow + firstRow;
    }

    sb.append("select * from ( ");

    sb.append("select ");
    if (request.isDistinct()) {
      sb.append("distinct ");
    }

    sb.append(ROW_NUMBER_OVER);
    sb.append(request.getDbOrderBy());
    sb.append(ROW_NUMBER_AS);

    sb.append(dbSql);

    sb.append(" ) ");
    sb.append(rowNumberWindowAlias);
    sb.append(" where ");
    if (firstRow > 0) {
      sb.append(" rn > ").append(firstRow);
      if (lastRow > 0) {
        sb.append(" and ");
      }
    }
    if (lastRow > 0) {
      sb.append(" rn <= ").append(lastRow);
    }

    String sql = request.getDbPlatform().completeSql(sb.toString(), request.getOrmQuery());

    return new SqlLimitResponse(sql, true);
  }
}
