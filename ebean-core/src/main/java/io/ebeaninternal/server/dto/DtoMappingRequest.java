package io.ebeaninternal.server.dto;

import io.ebean.ProfileLocation;
import io.ebean.metric.MetricFactory;
import io.ebean.metric.QueryPlanMetric;
import io.ebeaninternal.api.SpiDtoQuery;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.api.SpiQueryBindCapture;
import io.ebeaninternal.api.SpiQueryPlan;
import io.ebeaninternal.server.query.CQueryPlan;
import io.ebeaninternal.server.util.Md5;

/**
 * Request to map a resultSet columns for a query into a DTO bean.
 */
public final class DtoMappingRequest {

  private final SpiEbeanServer server;
  private final Class type;
  private final String label;
  private final ProfileLocation profileLocation;
  private final String sql;
  private final boolean relaxedMode;
  private final DtoColumn[] columnMeta;
  private final String name;
  private final String hash;

  public DtoMappingRequest(SpiEbeanServer server, SpiDtoQuery<?> query, String sql, DtoColumn[] columnMeta) {
    this.server = server;
    this.type = query.type();
    this.label = query.planLabel();
    this.profileLocation = query.profileLocation();
    this.sql = sql;
    this.relaxedMode = query.isRelaxedMode();
    this.columnMeta = columnMeta;
    this.name = deriveName(type.getSimpleName(), query.explicitLabel(), profileLocation);
    String loc = profileLocation == null ? null : profileLocation.location();
    this.hash = Md5.hash(sql, name, loc);
  }

  /**
   * Derive the DTO query plan / metric name, mirroring the ORM convention:
   * an explicit label is prefixed with the bean type for disambiguation, a
   * profile location is used as-is (already a unique, type-independent
   * identifier) and an unlabelled query uses just the bean type.
   */
  private static String deriveName(String simpleName, String explicitLabel, ProfileLocation profileLocation) {
    if (explicitLabel != null) {
      return "dto." + CQueryPlan.planLabelWithType(explicitLabel, simpleName);
    }
    if (profileLocation != null) {
      return "dto." + profileLocation.label();
    }
    return "dto." + simpleName;
  }

  public DtoColumn[] columnMeta() {
    return columnMeta;
  }

  public boolean relaxedMode() {
    return relaxedMode;
  }

  public String label() {
    return label;
  }

  public String sql() {
    return sql;
  }

  public Class<?> type() {
    return type;
  }

  public String name() {
    return name;
  }

  public String hash() {
    return hash;
  }

  public ProfileLocation profileLocation() {
    return profileLocation;
  }

  /**
   * Create the bind capture for the given (native SQL) query plan. Returns the
   * NOOP capture when query plan collection is disabled.
   */
  public SpiQueryBindCapture createBindCapture(SpiQueryPlan queryPlan) {
    return server.createQueryBindCapture(queryPlan);
  }

  public QueryPlanMetric createMetric() {
    return MetricFactory.get().createQueryPlanMetric(type, name, label, profileLocation, sql, hash);
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
    String label = columnMeta[pos].label();
    DtoReadSet property = meta.findProperty(label);
    if (property == null || property.isReadOnly()) {
      if (relaxedMode()) {
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
