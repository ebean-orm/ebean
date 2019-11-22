package io.ebeaninternal.server.deploy;

/**
 * 
 * A column in the DbSqlContext that we want to count distinctly in CQueryBuilder.
 *
 * @author Thomas Fellner, FOCONIS AG
 *
 */
public class DbSqlContextColumn {
  private final String sql;
  private final String alias;
  
  public DbSqlContextColumn(final String sql, final String alias) {
    this.sql = sql;
    this.alias = alias;
  }
  
  public String getAlias() {
    return alias;
  }
  
  public String getSql() {
    return sql;
  }
}
