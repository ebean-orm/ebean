package io.ebeaninternal.server.query;

import io.ebean.ProfileLocation;
import io.ebean.config.dbplatform.SqlLimitResponse;
import io.ebean.core.type.DataReader;
import io.ebean.core.type.ScalarDataReader;
import io.ebean.meta.MetricVisitor;
import io.ebean.metric.MetricFactory;
import io.ebean.metric.TimedMetric;
import io.ebeaninternal.api.*;
import io.ebeaninternal.server.core.OrmQueryRequest;
import io.ebeaninternal.server.core.timezone.DataTimeZone;
import io.ebeaninternal.server.query.CQueryPlanStats.Snapshot;
import io.ebeaninternal.server.bind.DataBind;
import io.ebeaninternal.server.bind.DataBindCapture;
import io.ebeaninternal.server.type.RsetDataReader;
import io.ebeaninternal.server.util.Md5;
import io.ebeaninternal.server.util.Str;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;

import static java.lang.System.Logger.Level.ERROR;

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
  private final SqlTreePlan sqlTree;

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
  CQueryPlan(OrmQueryRequest<?> request, SqlLimitResponse sqlRes, SqlTreePlan sqlTree, boolean rawSql, String logWhereSql) {
    this.server = request.server();
    this.dataTimeZone = server.dataTimeZone();
    this.beanType = request.descriptor().type();
    this.planKey = request.queryPlanKey();
    SpiQuery<?> query = request.query();
    this.profileLocation = query.profileLocation();
    this.location = (profileLocation == null) ? null : profileLocation.location();
    this.label = query.planLabel();
    this.name = deriveName(label, query.type(), request.descriptor().simpleName());
    this.asOfTableCount = query.getAsOfTableCount();
    this.sql = sqlRes.getSql();
    this.sqlTree = sqlTree;
    this.rawSql = rawSql;
    this.logWhereSql = logWhereSql;
    this.encryptedProps = sqlTree.encryptedProps();
    this.stats = new CQueryPlanStats(this);
    this.dependentTables = sqlTree.dependentTables();
    this.bindCapture = initBindCapture(query);
    this.hash = Md5.hash(sql, name, location);
  }

  /**
   * Create a query plan for a raw sql query.
   */
  CQueryPlan(OrmQueryRequest<?> request, String sql, SqlTreePlan sqlTree, String logWhereSql) {
    this.server = request.server();
    this.dataTimeZone = server.dataTimeZone();
    this.beanType = request.descriptor().type();
    SpiQuery<?> query = request.query();
    this.profileLocation = query.profileLocation();
    this.location = (profileLocation == null) ? null : profileLocation.location();
    this.label = query.planLabel();
    this.name = deriveName(label, query.type(), request.descriptor().simpleName());
    this.planKey = buildPlanKey(sql, logWhereSql);
    this.asOfTableCount = 0;
    this.sql = sql;
    this.sqlTree = sqlTree;
    this.rawSql = false;
    this.logWhereSql = logWhereSql;
    this.encryptedProps = sqlTree.encryptedProps();
    this.stats = new CQueryPlanStats(this);
    this.dependentTables = sqlTree.dependentTables();
    this.bindCapture = initBindCaptureRaw(sql, query);
    this.hash = Md5.hash(sql, name, location);
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
    return query.type().isUpdate() ? SpiQueryBindCapture.NOOP : server.createQueryBindCapture(this);
  }

  private SpiQueryBindCapture initBindCaptureRaw(String sql, SpiQuery<?> query) {
    return sql.equals(RESULT_SET_BASED_RAW_SQL) || query.type().isUpdate() ? SpiQueryBindCapture.NOOP : server.createQueryBindCapture(this);
  }

  private CQueryPlanKey buildPlanKey(String sql, String logWhereSql) {
    return new RawSqlQueryPlanKey(sql, false, logWhereSql);
  }

  @Override
  public String toString() {
    return beanType + " hash:" + planKey;
  }

  @Override
  public final Class<?> beanType() {
    return beanType;
  }

  @Override
  public final String name() {
    return name;
  }

  @Override
  public final String hash() {
    return hash;
  }

  @Override
  public final String sql() {
    return sql;
  }

  @Override
  public final ProfileLocation profileLocation() {
    return profileLocation;
  }

  public final String label() {
    return label;
  }

  public final Set<String> dependentTables() {
    return dependentTables;
  }

  public final String location() {
    return location;
  }

  @Override
  public final void queryPlanInit(long thresholdMicros) {
    bindCapture.queryPlanInit(thresholdMicros);
  }

  @Override
  public final DQueryPlanOutput createMeta(String bind, String planString) {
    return new DQueryPlanOutput(beanType(), name, hash, sql, profileLocation, bind, planString);
  }

  public DataReader createDataReader(boolean unmodifiable, ResultSet rset) {
    return new RsetDataReader(unmodifiable, dataTimeZone, rset);
  }

  /**
   * Bind keys for encrypted properties if necessary returning the DataBind.
   */
  final DataBind bindEncryptedProperties(PreparedStatement stmt, Connection conn) throws SQLException {
    DataBind dataBind = new DataBind(dataTimeZone, stmt, conn);
    if (encryptedProps != null) {
      for (STreeProperty encryptedProp : encryptedProps) {
        dataBind.setString(encryptedProp.encryptKeyAsString());
      }
    }
    return dataBind;
  }

  private DataBindCapture bindCapture() throws SQLException {
    DataBindCapture dataBind = DataBindCapture.of(dataTimeZone);
    if (encryptedProps != null) {
      for (STreeProperty encryptedProp : encryptedProps) {
        dataBind.setString(encryptedProp.encryptKeyAsString());
      }
    }
    return dataBind;
  }

  final int asOfTableCount() {
    return asOfTableCount;
  }

  /**
   * Return a key used in audit logging to identify the query.
   */
  final String auditQueryKey() {
    if (auditQueryHash == null) {
      // volatile object assignment (so happy for multithreaded access)
      auditQueryHash = calcAuditQueryKey();
    }
    return auditQueryHash;
  }

  private String calcAuditQueryKey() {
    // rawSql needs to include the MD5 hash of the sql
    return rawSql ? planKey.partialKey() + "_" + hash : planKey.partialKey();
  }

  final SqlTreePlan sqlTree() {
    return sqlTree;
  }

  public final boolean isRawSql() {
    return rawSql;
  }

  final String logWhereSql() {
    return logWhereSql;
  }

  /**
   * Reset the query statistics.
   */
  public final void resetStatistics() {
    stats.reset();
  }

  /**
   * Register an execution time against this query plan;
   */
  final boolean executionTime(long timeMicros) {
    stats.add(timeMicros);
    return bindCapture != null && bindCapture.collectFor(timeMicros);
  }

  /**
   * Return a copy of the current query statistics.
   */
  public final Snapshot visit(MetricVisitor visitor) {
    return stats.visit(visitor);
  }

  /**
   * Return the time this query plan was last used.
   */
  public final long lastQueryTime() {
    return stats.lastQueryTime();
  }

  final ScalarDataReader<?> singleAttributeScalarType() {
    return sqlTree.rootNode().singleAttributeReader();
  }

  /**
   * Return true if there are no statistics collected since the last reset.
   */
  public final boolean isEmptyStats() {
    return stats.isEmpty();
  }

  final TimedMetric createTimedMetric() {
    return MetricFactory.get().createTimedMetric(label);
  }

  final void captureBindForQueryPlan(CQueryPredicates predicates, long executionTimeMicros) {
    final long startNanos = System.nanoTime();
    try {
      DataBindCapture capture = bindCapture();
      predicates.bind(capture);
      bindCapture.setBind(capture.bindCapture(), executionTimeMicros, startNanos);
    } catch (SQLException e) {
      CoreLog.log.log(ERROR, "Error capturing bind values", e);
    }
  }
}
