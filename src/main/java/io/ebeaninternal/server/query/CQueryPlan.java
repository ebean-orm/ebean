package io.ebeaninternal.server.query;

import io.ebean.ProfileLocation;
import io.ebean.bean.ObjectGraphNode;
import io.ebean.config.dbplatform.SqlLimitResponse;
import io.ebean.meta.MetricType;
import io.ebeaninternal.api.CQueryPlanKey;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.api.SpiQuery;
import io.ebeaninternal.metric.MetricFactory;
import io.ebeaninternal.metric.TimedMetric;
import io.ebeaninternal.server.core.OrmQueryRequest;
import io.ebeaninternal.server.core.timezone.DataTimeZone;
import io.ebeaninternal.server.query.CQueryPlanStats.Snapshot;
import io.ebeaninternal.server.type.DataBind;
import io.ebeaninternal.server.type.DataReader;
import io.ebeaninternal.server.type.RsetDataReader;
import io.ebeaninternal.server.type.ScalarType;
import io.ebeaninternal.server.util.Md5;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Set;

/**
 * Represents a query for a given SQL statement.
 * <p>
 * This can be executed multiple times with different bind parameters.
 * </p>
 * <p>
 * That is, the sql including the where clause, order by clause etc must be
 * exactly the same to share the same query plan with the only difference being
 * bind values.
 * </p>
 * <p>
 * This is useful in that is common in OLTP type applications that the same
 * query will be executed quite a lot just with different bind values. With this
 * query plan we can bypass some of the query statement generation (for
 * performance) and collect statistics on the number and average execution
 * times. This is turn can be used to identify queries that could be looked at
 * for performance tuning.
 * </p>
 */
public class CQueryPlan {

  private static final Logger logger = LoggerFactory.getLogger(CQueryPlan.class);

  private final SpiEbeanServer server;

  private final boolean autoTuned;

  private final ProfileLocation profileLocation;

  private final String location;

  private final String label;

  private final CQueryPlanKey planKey;

  private final boolean rawSql;

  private final boolean rowNumberIncluded;

  private final String sql;

  private final String logWhereSql;

  private final SqlTree sqlTree;

  /**
   * Encrypted properties required additional binding.
   */
  private final STreeProperty[] encryptedProps;

  private final CQueryPlanStats stats;

  private final Class<?> beanType;

  protected final DataTimeZone dataTimeZone;

  private final int asOfTableCount;

  /**
   * Key used to identify the query plan in audit logging.
   */
  private volatile String auditQueryHash;

  private final Set<String> dependentTables;

  /**
   * Create a query plan based on a OrmQueryRequest.
   */
  CQueryPlan(OrmQueryRequest<?> request, SqlLimitResponse sqlRes, SqlTree sqlTree, boolean rawSql, String logWhereSql) {

    this.server = request.getServer();
    this.dataTimeZone = server.getDataTimeZone();
    this.beanType = request.getBeanDescriptor().getBeanType();
    this.planKey = request.getQueryPlanKey();
    SpiQuery<?> query = request.getQuery();
    this.profileLocation = query.getProfileLocation();
    this.label = query.getLabel();
    this.location = location();
    this.autoTuned = query.isAutoTuned();
    this.asOfTableCount = query.getAsOfTableCount();
    this.sql = sqlRes.getSql();
    this.rowNumberIncluded = sqlRes.isIncludesRowNumberColumn();
    this.sqlTree = sqlTree;
    this.rawSql = rawSql;
    this.logWhereSql = logWhereSql;
    this.encryptedProps = sqlTree.getEncryptedProps();
    this.stats = new CQueryPlanStats(this, server.isCollectQueryOrigins());
    this.dependentTables = sqlTree.dependentTables();
  }

  /**
   * Create a query plan for a raw sql query.
   */
  CQueryPlan(OrmQueryRequest<?> request, String sql, SqlTree sqlTree, boolean rawSql, boolean rowNumberIncluded, String logWhereSql) {

    this.server = request.getServer();
    this.dataTimeZone = server.getDataTimeZone();
    this.beanType = request.getBeanDescriptor().getBeanType();
    SpiQuery<?> query = request.getQuery();
    this.profileLocation = query.getProfileLocation();
    this.label = query.getLabel();
    this.location = location();
    this.planKey = buildPlanKey(sql, rawSql, rowNumberIncluded, logWhereSql);
    this.autoTuned = false;
    this.asOfTableCount = 0;
    this.sql = sql;
    this.sqlTree = sqlTree;
    this.rawSql = rawSql;
    this.rowNumberIncluded = rowNumberIncluded;
    this.logWhereSql = logWhereSql;
    this.encryptedProps = sqlTree.getEncryptedProps();
    this.stats = new CQueryPlanStats(this, server.isCollectQueryOrigins());
    this.dependentTables = (rawSql) ? Collections.emptySet() : sqlTree.dependentTables();
  }

  private String location() {
    return (profileLocation == null) ? "" : profileLocation.shortDescription();
  }

  private CQueryPlanKey buildPlanKey(String sql, boolean rawSql, boolean rowNumberIncluded, String logWhereSql) {

    return new RawSqlQueryPlanKey(sql, rawSql, rowNumberIncluded, logWhereSql);
  }

  @Override
  public String toString() {
    return beanType + " hash:" + planKey;
  }

  public Class<?> getBeanType() {
    return beanType;
  }

  public Set<String> getDependentTables() {
    return dependentTables;
  }

  public ProfileLocation getProfileLocation() {
    return profileLocation;
  }

  public String getLabel() {
    return label;
  }

  public String getLocation() {
    return location;
  }

  public DataReader createDataReader(ResultSet rset) {
    return new RsetDataReader(dataTimeZone, rset);
  }

  /**
   * Bind keys for encrypted properties if necessary returning the DataBind.
   */
  DataBind bindEncryptedProperties(PreparedStatement stmt, Connection conn) throws SQLException {
    DataBind dataBind = new DataBind(dataTimeZone, stmt, conn);
    if (encryptedProps != null) {
      for (STreeProperty encryptedProp : encryptedProps) {
        dataBind.setString(encryptedProp.getEncryptKeyAsString());
      }
    }
    return dataBind;
  }

  int getAsOfTableCount() {
    return asOfTableCount;
  }

  boolean isAutoTuned() {
    return autoTuned;
  }

  CQueryPlanKey getPlanKey() {
    return planKey;
  }

  /**
   * Return a key used in audit logging to identify the query.
   */
  String getAuditQueryKey() {
    if (auditQueryHash == null) {
      // volatile object assignment (so happy for multithreaded access)
      auditQueryHash = calcAuditQueryKey();
    }
    return auditQueryHash;
  }

  private String calcAuditQueryKey() {
    // rawSql needs to include the MD5 hash of the sql
    return rawSql ? planKey.getPartialKey() + "_" + getSqlMd5Hash() : planKey.getPartialKey();
  }

  /**
   * Return the MD5 hash of the underlying sql.
   */
  private String getSqlMd5Hash() {
    try {
      return Md5.hash(sql);
    } catch (Exception e) {
      logger.error("Failed to MD5 hash the rawSql query", e);
      return "error";
    }
  }

  public String getSql() {
    return sql;
  }

  SqlTree getSqlTree() {
    return sqlTree;
  }

  public boolean isRawSql() {
    return rawSql;
  }

  boolean isRowNumberIncluded() {
    return rowNumberIncluded;
  }

  String getLogWhereSql() {
    return logWhereSql;
  }

  /**
   * Reset the query statistics.
   */
  public void resetStatistics() {
    stats.reset();
  }

  /**
   * Register an execution time against this query plan;
   */
  void executionTime(long loadedBeanCount, long timeMicros, ObjectGraphNode objectGraphNode) {

    stats.add(loadedBeanCount, timeMicros, objectGraphNode);
    if (objectGraphNode != null) {
      // collect stats based on objectGraphNode for lazy loading reporting
      server.collectQueryStats(objectGraphNode, loadedBeanCount, timeMicros);
    }
  }

  /**
   * Return a copy of the current query statistics.
   */
  public Snapshot getSnapshot(boolean reset) {
    return stats.getSnapshot(reset);
  }

  /**
   * Return the time this query plan was last used.
   */
  public long getLastQueryTime() {
    return stats.getLastQueryTime();
  }

  ScalarType<?> getSingleAttributeScalarType() {
    return sqlTree.getRootNode().getSingleAttributeScalarType();
  }

  /**
   * Return true if there are no statistics collected since the last reset.
   */
  public boolean isEmptyStats() {
    return stats.isEmpty();
  }

  public TimedMetric createTimedMetric() {
    return MetricFactory.get().createTimedMetric(MetricType.ORM, label);
  }
}
