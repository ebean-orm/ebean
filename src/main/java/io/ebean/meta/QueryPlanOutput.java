package io.ebean.meta;

/**
 * Captured query plan details.
 */
public class QueryPlanOutput {

  private final Class<?> beanType;
  private final String label;

  private final String sql;

  private final String bind;

  private final String plan;

  private long queryTimeMicros;
  private long captureCount;

  public QueryPlanOutput(Class<?> beanType, String label, String sql, String bind, String plan) {
    this.beanType = beanType;
    this.label = label;
    this.sql = sql;
    this.bind = bind;
    this.plan = plan;
  }

  /**
   * Return the associated bean.
   */
  public Class<?> getBeanType() {
    return beanType;
  }

  /**
   * Return the query label if set.
   */
  public String getLabel() {
    return label;
  }

  /**
   * Return the sql of query.
   */
  public String getSql() {
    return sql;
  }

  /**
   * Return a description of the bind values used.
   */
  public String getBind() {
    return bind;
  }

  /**
   * Return the query plan.
   */
  public String getPlan() {
    return plan;
  }

  /**
   * Return the query execution time associated with the capture of bind values used
   * to build the query plan.
   */
  public long getQueryTimeMicros() {
    return queryTimeMicros;
  }

  /**
   * Return the total count of times bind capture has occurred. We don't want this to be
   * massive as that implies a high overhead.
   */
  public long getCaptureCount() {
    return captureCount;
  }

  @Override
  public String toString() {
    return " BeanType:" + ((beanType == null) ? "" : beanType.getSimpleName()) + " label:" + label + " queryTimeMicros:" + queryTimeMicros + " captureCount:" + captureCount + "\n SQL:" + sql + "\nBIND:" + bind + "\nPLAN:" + plan;
  }

  /**
   * Additionally set the query execution time and the number of bind captures.
   */
  public QueryPlanOutput with(long queryTimeMicros, long captureCount) {
    this.queryTimeMicros = queryTimeMicros;
    this.captureCount = captureCount;
    return this;
  }
}
