package io.ebean.config.dbplatform;

/**
 * Adds ROWNUM based limit clauses to a SQL query.
 */
public class BasicSqlRowNumLimiter implements BasicSqlLimiter {

  @Override
  public String limit(String dbSql, int firstRow, int maxRows) {

    StringBuilder sb = new StringBuilder(100 + dbSql.length());
 
    int lastRow = maxRows;
    if (lastRow > 0) {
      lastRow += firstRow;
    }
    
    sb.append("select * from (select a.*, rownum rn_ from ( ").append(dbSql).append(") a");
    if (lastRow > 0) {
      sb.append(" where rownum <= ").append(lastRow);
    }
    sb.append(')');
    if (firstRow > 0) {
      sb.append(" where rn_ > ").append(firstRow);
    }

    return sb.toString();
  }

}
