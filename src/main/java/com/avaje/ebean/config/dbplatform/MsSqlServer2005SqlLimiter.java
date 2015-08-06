package com.avaje.ebean.config.dbplatform;

/**
 * Use top and row_number() function to limit sql results.
 */
public class MsSqlServer2005SqlLimiter implements SqlLimiter {

  final String rowNumberWindowAlias;

  /**
   * Specify the name of the rowNumberWindowAlias.
   */
  public MsSqlServer2005SqlLimiter(String rowNumberWindowAlias) {
    this.rowNumberWindowAlias = rowNumberWindowAlias;
  }

  public MsSqlServer2005SqlLimiter() {
    this("as limitresult");
  }

  public SqlLimitResponse limit(SqlLimitRequest request) {

    StringBuilder sb = new StringBuilder(500);

    int firstRow = request.getFirstRow();

    int lastRow = request.getMaxRows();
    if (lastRow > 0) {
      lastRow = lastRow + firstRow;
    }

    if (firstRow < 1) {
      // just use top n
      sb.append(" select ");
      if (request.isDistinct()) {
        sb.append("distinct ");
      }
      sb.append(" top ").append(lastRow).append(" ");
      sb.append(request.getDbSql());
      return new SqlLimitResponse(sb.toString(), false);
    }

    /*
     * SELECT * FROM (SELECT TOP 20 ROW_NUMBER() OVER (ORDER BY ...) AS rn, ...)
     * AS limitresult WHERE rn >= 11 AND rn <= 20
     */

    sb.append("select * ").append(NEW_LINE).append("from ( ");

    sb.append("select ");
    if (request.isDistinct()) {
      sb.append("distinct ");
    }
    sb.append("top ").append(lastRow);
    sb.append(" row_number() over (order by ");
    sb.append(request.getDbOrderBy());
    sb.append(") as rn, ");
    sb.append(request.getDbSql());

    sb.append(NEW_LINE).append(") ");
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
