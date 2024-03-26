package io.ebeaninternal.server.core;

import io.ebean.ProfileLocation;
import io.ebean.bean.ObjectGraphNode;
import io.ebean.config.SlowQueryEvent;

import java.util.List;

/**
 * Slow query event.
 */
final class DSlowQueryEvent implements SlowQueryEvent {

  private final String sql;
  private final long timeMillis;
  private final int rowCount;
  private final ObjectGraphNode originNode;
  private final List<Object> bindParams;
  private final String label;
  private final ProfileLocation profileLocation;

  /**
   * Construct with the SQL and execution time in millis.
   */
  DSlowQueryEvent(String sql, long timeMillis, int rowCount, ObjectGraphNode originNode,
                         List<Object> bindParams, String label, ProfileLocation profileLocation) {
    this.sql = sql;
    this.timeMillis = timeMillis;
    this.rowCount = rowCount;
    this.originNode = originNode;
    this.bindParams = bindParams;
    this.profileLocation = profileLocation;
    this.label = label != null ? label : profileLocation == null ? null : profileLocation.label();
  }

  @Override
  public String getSql() {
    return sql;
  }

  @Override
  public long getTimeMillis() {
    return timeMillis;
  }

  @Override
  public int getRowCount() {
    return rowCount;
  }

  @Override
  public ObjectGraphNode getOriginNode() {
    return originNode;
  }

  @Override
  public List<Object> getBindParams() {
    return bindParams;
  }

  @Override
  public String getLabel() {
    return label;
  }

  @Override
  public ProfileLocation getProfileLocation() {
    return profileLocation;
  }
}
