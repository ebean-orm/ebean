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

  /**
   * Map all DB columns to setters.
   */
  DtoReadSet[] mapSetters(DtoMeta meta) {
    DtoReadSet[] setterProps = new DtoReadSet[columnMeta.length];
    for (int i = 0; i < columnMeta.length; i++) {
      setterProps[i] = mapColumn(i, meta);
    }
    return setterProps;
  }

  /**
   * Map DB columns after constructor to setters.
   */
  DtoReadSet[] mapArgPlusSetters(DtoMeta meta, int firstOnes) {
    DtoReadSet[] setterProps = new DtoReadSet[columnMeta.length - firstOnes];
    int pos = 0;
    for (int i = firstOnes; i < columnMeta.length; i++) {
      setterProps[pos++] = mapColumn(i, meta);
    }
    return setterProps;
  }

  private DtoReadSet mapColumn(int pos, DtoMeta meta) {
    String label = columnMeta[pos].getLabel();
    DtoReadSet property = meta.findProperty(label);
    if (property == null || property.isReadOnly()) {
      if (isRelaxedMode()) {
        property = DtoReadSetColumnSkip.INSTANCE;
      } else {
        throw new IllegalStateException(unableToMapColumnMessage(columnMeta[pos], meta));
      }
    }
    return property;
  }

  private String unableToMapColumnMessage(DtoColumn col, DtoMeta meta) {
    return "Unable to map DB column " + col + " to a property with a setter method on " + meta.dtoType()+". Consider query.setRelaxedMode() to skip mapping this column.";
  }

}
