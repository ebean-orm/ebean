package io.ebeaninternal.server.dto;

import io.ebean.ProfileLocation;
import io.ebean.metric.MetricFactory;
import io.ebean.metric.QueryPlanMetric;
import io.ebeaninternal.api.SpiDtoQuery;

/**
 * Request to map a resultSet columns for a query into a DTO bean.
 */
public class DtoMappingRequest {

  private final Class type;

  private final String label;

  private final ProfileLocation profileLocation;

  private final String sql;

  private final boolean relaxedMode;

  private final DtoColumn[] columnMeta;

  public DtoMappingRequest(SpiDtoQuery query, String sql, DtoColumn[] columnMeta) {
    this.type = query.getType();
    this.label = query.getPlanLabel();
    this.profileLocation = query.getProfileLocation();
    this.sql = sql;
    this.relaxedMode = query.isRelaxedMode();
    this.columnMeta = columnMeta;
  }

  public DtoColumn[] getColumnMeta() {
    return columnMeta;
  }

  public boolean isRelaxedMode() {
    return relaxedMode;
  }

  public String getLabel() {
    return label;
  }

  public String getSql() {
    return sql;
  }

  public QueryPlanMetric createMetric() {
    return MetricFactory.get().createQueryPlanMetric(type, label, profileLocation, sql);
  }
}
