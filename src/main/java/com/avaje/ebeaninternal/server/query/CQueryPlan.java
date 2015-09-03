package com.avaje.ebeaninternal.server.query;

import java.security.MessageDigest;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.avaje.ebean.bean.ObjectGraphNode;
import com.avaje.ebean.config.dbplatform.SqlLimitResponse;
import com.avaje.ebeaninternal.api.HashQueryPlan;
import com.avaje.ebeaninternal.api.HashQueryPlanBuilder;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.ebeaninternal.server.core.OrmQueryRequest;
import com.avaje.ebeaninternal.server.deploy.BeanProperty;
import com.avaje.ebeaninternal.server.query.CQueryPlanStats.Snapshot;
import com.avaje.ebeaninternal.server.type.DataBind;
import com.avaje.ebeaninternal.server.type.DataReader;
import com.avaje.ebeaninternal.server.type.RsetDataReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

  private final boolean autofetchTuned;

  private final HashQueryPlan hash;

  private final boolean rawSql;

  private final boolean rowNumberIncluded;

  private final String sql;

  private final String logWhereSql;

  private final SqlTree sqlTree;

  /**
   * Encrypted properties required additional binding.
   */
  private final BeanProperty[] encryptedProps;

  private final CQueryPlanStats stats;

  private final Class<?> beanType;

  /**
   * Key used to identify the query plan in audit logging.
   */
  private volatile String auditQueryHash;

  /**
   * Create a query plan based on a OrmQueryRequest.
   */
  public CQueryPlan(OrmQueryRequest<?> request, SqlLimitResponse sqlRes, SqlTree sqlTree, boolean rawSql, String logWhereSql) {

    this.server = request.getServer();
    this.beanType = request.getBeanDescriptor().getBeanType();
    this.stats = new CQueryPlanStats(this, server.isCollectQueryOrigins());
    this.hash = request.getQueryPlanHash();
    this.autofetchTuned = request.getQuery().isAutofetchTuned();
    if (sqlRes != null) {
      this.sql = sqlRes.getSql();
      this.rowNumberIncluded = sqlRes.isIncludesRowNumberColumn();
    } else {
      this.sql = null;
      this.rowNumberIncluded = false;
    }
    this.sqlTree = sqlTree;
    this.rawSql = rawSql;
    this.logWhereSql = logWhereSql;
    this.encryptedProps = sqlTree.getEncryptedProps();
  }

  /**
   * Create a query plan for a raw sql query.
   */
  public CQueryPlan(OrmQueryRequest<?> request, String sql, SqlTree sqlTree,
                    boolean rawSql, boolean rowNumberIncluded, String logWhereSql) {

    this.server = request.getServer();
    this.beanType = request.getBeanDescriptor().getBeanType();
    this.stats = new CQueryPlanStats(this, server.isCollectQueryOrigins());
    this.hash = buildHash(sql, rawSql, rowNumberIncluded, logWhereSql);
    this.autofetchTuned = false;
    this.sql = sql;
    this.sqlTree = sqlTree;
    this.rawSql = rawSql;
    this.rowNumberIncluded = rowNumberIncluded;
    this.logWhereSql = logWhereSql;
    this.encryptedProps = sqlTree.getEncryptedProps();
  }


  private HashQueryPlan buildHash(String sql, boolean rawSql, boolean rowNumberIncluded, String logWhereSql) {
    HashQueryPlanBuilder builder = new HashQueryPlanBuilder();
    builder.add(sql).add(rawSql).add(rowNumberIncluded).add(logWhereSql);
    builder.addRawSql(sql);
    return builder.build();
  }

  public String toString() {
    return beanType + " hash:" + hash;
  }

  public Class<?> getBeanType() {
    return beanType;
  }

  public DataReader createDataReader(ResultSet rset) {

    return new RsetDataReader(rset);
  }

  public void bindEncryptedProperties(DataBind dataBind) throws SQLException {
    if (encryptedProps != null) {
      for (int i = 0; i < encryptedProps.length; i++) {
        String key = encryptedProps[i].getEncryptKey().getStringValue();
        dataBind.setString(key);
      }
    }
  }

  public boolean isAutofetchTuned() {
    return autofetchTuned;
  }

  public HashQueryPlan getHash() {
    return hash;
  }

  /**
   * Return a key used in audit logging to identify the query.
   */
  public String getAuditQueryKey() {
    if (auditQueryHash == null) {
      // volatile object assignment (so happy for multithreaded access)
      auditQueryHash = calcAuditQueryKey();
    }
    return auditQueryHash;
  }

  private String calcAuditQueryKey() {
    // rawSql needs to include the MD5 hash of the sql
    return rawSql ? hash.getPartialKey() + "_" + getSqlMd5Hash() : hash.getPartialKey();
  }

  /**
   * Return the MD5 hash of the underlying sql.
   */
  private String getSqlMd5Hash() {
    try {
      MessageDigest md = MessageDigest.getInstance("MD5");
      byte[] digest = md.digest(sql.getBytes("UTF-8"));
      return digestToHex(digest);
    } catch (Exception e) {
      logger.error("Failed to MD5 hash the rawSql query", e);
      return "error";
    }
  }

  /**
   * Convert the digest into a hex value.
   */
  private String digestToHex(byte[] digest) {

    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < digest.length; i++) {
      sb.append(Integer.toString((digest[i] & 0xff) + 0x100, 16).substring(1));
    }
    return sb.toString();
  }

  public String getSql() {
    return sql;
  }

  public SqlTree getSqlTree() {
    return sqlTree;
  }

  public boolean isRawSql() {
    return rawSql;
  }

  public boolean isRowNumberIncluded() {
    return rowNumberIncluded;
  }

  public String getLogWhereSql() {
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
  public void executionTime(long loadedBeanCount, long timeMicros, ObjectGraphNode objectGraphNode) {

    stats.add(loadedBeanCount, timeMicros, objectGraphNode);
    if (objectGraphNode != null) {
      // collect stats based on objectGraphNode for lazy loading reporting
      server.collectQueryStats(objectGraphNode, loadedBeanCount, timeMicros);
    }
  }

  public Snapshot getSnapshot(boolean reset) {
    return stats.getSnapshot(reset);
  }

  /**
   * Return the current query statistics.
   */
  public CQueryPlanStats getQueryStats() {
    return stats;
  }

  /**
   * Return the time this query plan was last used.
   */
  public long getLastQueryTime() {
    return stats.getLastQueryTime();
  }

}
