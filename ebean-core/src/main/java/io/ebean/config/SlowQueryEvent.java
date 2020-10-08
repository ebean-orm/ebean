package io.ebean.config;

import io.ebean.bean.ObjectGraphNode;

/**
 * Slow query event.
 */
public class SlowQueryEvent {

  private final String sql;

  private final long timeMillis;

  private final int rowCount;

  private final ObjectGraphNode originNode;

  /**
   * Construct with the SQL and execution time in millis.
   */
  public SlowQueryEvent(String sql, long timeMillis, int rowCount, ObjectGraphNode originNode) {
    this.sql = sql;
    this.timeMillis = timeMillis;
    this.rowCount = rowCount;
    this.originNode = originNode;
  }

  /**
   * Return the SQL for the slow query.
   */
  public String getSql() {
    return sql;
  }

  /**
   * Return the execution time in millis.
   */
  public long getTimeMillis() {
    return timeMillis;
  }

  /**
   * Return the total row count associated with the query.
   */
  public int getRowCount() {
    return rowCount;
  }

  /**
   * Return the origin point for the root query.
   * <p>
   * Typically the <code>originNode.getOriginQueryPoint().getFirstStackElement()</code> provides the stack line that
   * shows the code that invoked the query.
   * </p>
   */
  public ObjectGraphNode getOriginNode() {
    return originNode;
  }
}
