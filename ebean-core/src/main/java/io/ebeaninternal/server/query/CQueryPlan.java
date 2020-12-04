package io.ebeaninternal.server.query;

import io.ebean.ProfileLocation;
import io.ebean.config.dbplatform.SqlLimitResponse;
import io.ebean.core.type.DataReader;
import io.ebean.core.type.ScalarDataReader;
import io.ebean.metric.MetricFactory;
import io.ebean.metric.TimedMetric;
import io.ebeaninternal.api.CQueryPlanKey;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.api.SpiQuery;
import io.ebeaninternal.api.SpiQueryBindCapture;
import io.ebeaninternal.api.SpiQueryPlan;
import io.ebeaninternal.server.core.OrmQueryRequest;
import io.ebeaninternal.server.core.timezone.DataTimeZone;
import io.ebeaninternal.server.lib.Str;
import io.ebeaninternal.server.query.CQueryPlanStats.Snapshot;
import io.ebeaninternal.server.type.DataBind;
import io.ebeaninternal.server.type.DataBindCapture;
import io.ebeaninternal.server.type.RsetDataReader;
import io.ebeaninternal.server.util.Md5;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
public class CQueryPlan implements SpiQueryPlan {

  private static final Logger logger = LoggerFactory.getLogger(CQueryPlan.class);

  static final String RESULT_SET_BASED_RAW_SQL = "--ResultSetBasedRawSql";

  private final SpiEbeanServer server;

  private final ProfileLocation profileLocation;

  private final String location;

  private final String label;

  private final String name;

  private final CQueryPlanKey planKey;

  private final boolean rawSql;

  private final String sql;
  private final String hash;

  private final String logWhereSql;

  private final SqlTree sqlTree;

  /**
   * Encrypted properties required additional binding.
   */
  private final STreeProperty[] encryptedProps;

  private final CQueryPlanStats stats;

  private final Class<?> beanType;

  final DataTimeZone dataTimeZone;

  private final int asOfTableCount;

  /**
   * Key used to identify the query plan in audit logging.
   */
  private volatile String auditQueryHash;

  private final Set<String> dependentTables;

  private final SpiQueryBindCapture bindCapture;

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
    this.label = query.getPlanLabel();
    this.name = deriveName(label, query.getType(), request.getBeanDescriptor().getSimpleName());
    this.location = location();
    this.asOfTableCount = query.getAsOfTableCount();
    this.sql = sqlRes.getSql();
    this.sqlTree = sqlTree;
    this.rawSql = rawSql;
    this.logWhereSql = logWhereSql;
    this.encryptedProps = sqlTree.getEncryptedProps();
    this.stats = new CQueryPlanStats(this);
    this.dependentTables = sqlTree.dependentTables();
    this.bindCapture = initBindCapture(query);
    this.hash = md5Hash();
  }

  /**
   * Create a query plan for a raw sql query.
   */
  CQueryPlan(OrmQueryRequest<?> request, String sql, SqlTree sqlTree, String logWhereSql) {
    this.server = request.getServer();
    this.dataTimeZone = server.getDataTimeZone();
    this.beanType = request.getBeanDescriptor().getBeanType();
    SpiQuery<?> query = request.getQuery();
    this.profileLocation = query.getProfileLocation();
    this.label = query.getPlanLabel();
    this.name = deriveName(label, query.getType(), request.getBeanDescriptor().getSimpleName());
    this.location = location();
    this.planKey = buildPlanKey(sql, logWhereSql);
    this.asOfTableCount = 0;
    this.sql = sql;
    this.sqlTree = sqlTree;
    this.rawSql = false;
    this.logWhereSql = logWhereSql;
    this.encryptedProps = sqlTree.getEncryptedProps();
    this.stats = new CQueryPlanStats(this);
    this.dependentTables = sqlTree.dependentTables();
    this.bindCapture = initBindCaptureRaw(sql, query);
    this.hash = md5Hash();
  }

  private String deriveName(String label, SpiQuery.Type type, String simpleName) {
    if (label == null) {
      return Str.add("orm.", simpleName, ".", type.label());
    }
    int pos = simpleName.indexOf('.');
    if (pos > 1) {
      // element collection and label
      return Str.add("orm.", simpleName.substring(0, pos), "_", label);
    }
    if (label.startsWith(simpleName)) {
      return Str.add("orm.", label);
    }
    return Str.add("orm.", simpleName, "_", label);
  }

  private SpiQueryBindCapture initBindCapture(SpiQuery<?> query) {
    return query.getType().isUpdate() ? SpiQueryBindCapture.NOOP : server.createQueryBindCapture(this);
  }

  private SpiQueryBindCapture initBindCaptureRaw(String sql, SpiQuery<?> query) {
    return sql.equals(RESULT_SET_BASED_RAW_SQL) || query.getType().isUpdate() ? SpiQueryBindCapture.NOOP : server.createQueryBindCapture(this);
  }

  private String location() {
    return (profileLocation == null) ? null : profileLocation.location();
  }

  private CQueryPlanKey buildPlanKey(String sql, String logWhereSql) {
    return new RawSqlQueryPlanKey(sql, false, logWhereSql);
  }

  @Override
  public String toString() {
    return beanType + " hash:" + planKey;
  }

  @Override
  public Class<?> getBeanType() {
    return beanType;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getHash() {
    return hash;
  }

  @Override
  public String getSql() {
    return sql;
  }

  @Override
  public ProfileLocation getProfileLocation() {
    return profileLocation;
  }

  public String getLabel() {
    return label;
  }

  public Set<String> getDependentTables() {
    return dependentTables;
  }

  public String getLocation() {
    return location;
  }

  @Override
  public void queryPlanInit(long thresholdMicros) {
    bindCapture.queryPlanInit(thresholdMicros);
  }

  @Override
  public DQueryPlanOutput createMeta(String bind, String planString) {
    return new DQueryPlanOutput(getBeanType(), name, hash, sql, profileLocation, bind, planString);
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

  private DataBindCapture bindCapture() throws SQLException {
    DataBindCapture dataBind = DataBindCapture.of(dataTimeZone);
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
    return rawSql ? planKey.getPartialKey() + "_" + hash : planKey.getPartialKey();
  }

  /**
   * Return the MD5 hash of the sql.
   */
  private String md5Hash() {
    StringBuilder sb = new StringBuilder(sql)
      .append("|").append(name)
      .append("|").append(location);
    try {
      return Md5.hash(sb.toString());
    } catch (Exception e) {
      logger.error("Failed to MD5 hash the query", e);
      return "error";
    }
  }

  SqlTree getSqlTree() {
    return sqlTree;
  }

  public boolean isRawSql() {
    return rawSql;
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
  boolean executionTime(long timeMicros) {
    stats.add(timeMicros);
    return bindCapture != null && bindCapture.collectFor(timeMicros);
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

  ScalarDataReader<?> getSingleAttributeScalarType() {
    return sqlTree.getRootNode().getSingleAttributeReader();
  }

  /**
   * Return true if there are no statistics collected since the last reset.
   */
  public boolean isEmptyStats() {
    return stats.isEmpty();
  }

  TimedMetric createTimedMetric() {
    return MetricFactory.get().createTimedMetric(label);
  }

  void captureBindForQueryPlan(CQueryPredicates predicates, long executionTimeMicros) {
    final long startNanos = System.nanoTime();
    try {
      DataBindCapture capture = bindCapture();
      predicates.bind(capture);
      bindCapture.setBind(capture.bindCapture(), executionTimeMicros, startNanos);
    } catch (SQLException e) {
      logger.error("Error capturing bind values", e);
    }
  }
}
