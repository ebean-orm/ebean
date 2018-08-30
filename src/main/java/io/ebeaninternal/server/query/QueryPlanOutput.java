package io.ebeaninternal.server.query;

public class QueryPlanOutput {

  private long whenCaptured;

  private final String sql;

  private final String bind;

  private final String plan;

  public QueryPlanOutput(String sql, String bind, String plan) {
    this.whenCaptured = System.currentTimeMillis();
    this.sql = sql;
    this.bind = bind;
    this.plan = plan;
  }

  public long getWhenCaptured() {
    return whenCaptured;
  }

  public String getSql() {
    return sql;
  }

  public String getBind() {
    return bind;
  }

  public String getPlan() {
    return plan;
  }

  @Override
  public String toString() {
    return " SQL:" + sql + "\nBIND:" + bind + "\nPLAN:" + plan;
  }
}
