package io.ebeaninternal.server.core;

import io.ebean.EbeanServer;
import io.ebean.SqlQuery;
import io.ebean.SqlRow;
import io.ebean.Transaction;
import io.ebeaninternal.api.BindParams;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.api.SpiSqlQuery;
import io.ebeaninternal.api.SpiTransaction;
import io.ebeaninternal.server.lib.util.Str;
import io.ebeaninternal.server.persist.Binder;
import io.ebeaninternal.server.persist.TrimLogSql;
import io.ebeaninternal.server.query.DefaultSqlRow;
import io.ebeaninternal.server.transaction.TransactionManager;
import io.ebeaninternal.server.util.BindParamsParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Wraps the objects involved in executing a SqlQuery.
 */
public final class RelationalQueryRequest {

  private static final Logger logger = LoggerFactory.getLogger(RelationalQueryRequest.class);

  private final SpiSqlQuery query;

  private final RelationalQueryEngine queryEngine;

  private final SpiEbeanServer ebeanServer;

  private SpiTransaction trans;

  private boolean createdTransaction;

  private String sql;

  private ResultSet resultSet;

  private int rowCount;

  private String bindLog = "";

  private String[] propertyNames;

  private int estimateCapacity;

  private PreparedStatement pstmt;

  /**
   * Create the BeanFindRequest.
   */
  RelationalQueryRequest(SpiEbeanServer server, RelationalQueryEngine engine, SqlQuery q, Transaction t) {
    this.ebeanServer = server;
    this.queryEngine = engine;
    this.query = (SpiSqlQuery) q;
    this.trans = (SpiTransaction) t;
  }

  /**
   * Create a transaction if none currently exists.
   */
  public void initTransIfRequired() {
    if (trans == null) {
      trans = ebeanServer.currentServerTransaction();
      if (trans == null || !trans.isActive()) {
        // create a local readOnly transaction
        trans = ebeanServer.beginServerTransaction();
        createdTransaction = true;
      }
    }
  }

  /**
   * End the transaction if it was locally created.
   */
  public void endTransIfRequired() {
    if (createdTransaction) {
      ebeanServer.commitTransaction();
    }
  }

  public void findEach(Consumer<SqlRow> consumer) {
    queryEngine.findEach(this, consumer);
  }

  public void findEachWhile(Predicate<SqlRow> consumer) {
    queryEngine.findEach(this, consumer);
  }

  public List<SqlRow> findList() {
    return queryEngine.findList(this);
  }

  /**
   * Return the find that is to be performed.
   */
  public SpiSqlQuery getQuery() {
    return query;
  }

  public EbeanServer getEbeanServer() {
    return ebeanServer;
  }

  public SpiTransaction getTransaction() {
    return trans;
  }

  public boolean isLogSql() {
    return trans.isLogSql();
  }

  public boolean isLogSummary() {
    return trans.isLogSummary();
  }

  private void setResultSet(ResultSet resultSet) throws SQLException {
    this.resultSet = resultSet;
    this.propertyNames = getPropertyNames();
    // calculate the initialCapacity of the Map to reduce rehashing
    float initCap = (propertyNames.length) / 0.7f;
    this.estimateCapacity = (int) initCap + 1;
  }

  /**
   * Build the list of property names.
   */
  private String[] getPropertyNames() throws SQLException {

    ResultSetMetaData metaData = resultSet.getMetaData();

    int columnsPlusOne = metaData.getColumnCount() + 1;
    ArrayList<String> propNames = new ArrayList<>(columnsPlusOne - 1);
    for (int i = 1; i < columnsPlusOne; i++) {
      propNames.add(metaData.getColumnLabel(i));
    }
    return propNames.toArray(new String[propNames.size()]);
  }

  /**
   * Return the bindLog for this request.
   */
  public String getBindLog() {
    return bindLog;
  }

  /**
   * Return true if we can navigate to the next row.
   */
  public boolean next() throws SQLException {
    rowCount++;
    return resultSet.next();
  }

  /**
   * Close the underlying resources.
   */
  public void close() {
    try {
      if (resultSet != null) {
        resultSet.close();
      }
    } catch (SQLException e) {
      logger.error(null, e);
    }
    try {
      if (pstmt != null) {
        pstmt.close();
      }
    } catch (SQLException e) {
      logger.error(null, e);
    }
  }

  /**
   * Read and return the next SqlRow.
   */
  public SqlRow createNewRow(String dbTrueValue) throws SQLException {

    SqlRow sqlRow = new DefaultSqlRow(estimateCapacity, 0.75f, dbTrueValue);

    int index = 0;
    for (String propertyName : propertyNames) {
      index++;
      Object value = resultSet.getObject(index);
      sqlRow.set(propertyName, value);
    }
    return sqlRow;
  }

  /**
   * Prepare the SQL taking into account named bind parameters.
   */
  private void prepareSql() {

    String sql = query.getQuery();
    BindParams bindParams = query.getBindParams();
    if (!bindParams.isEmpty()) {
      // convert any named parameters if required
      sql = BindParamsParser.parse(bindParams, sql);
    }
    this.sql = limitOffset(sql);
  }

  private String limitOffset(String sql) {

    int firstRow = query.getFirstRow();
    int maxRows = query.getMaxRows();
    if (firstRow > 0 || maxRows > 0) {
      return ebeanServer.getDatabasePlatform().getBasicSqlLimiter().limit(sql, firstRow, maxRows);
    }
    return sql;
  }

  /**
   * Prepare and execute the SQL using the Binder.
   */
  public void executeSql(Binder binder) throws SQLException {

    prepareSql();

    Connection conn = trans.getInternalConnection();

    // synchronise for query.cancel() support
    pstmt = conn.prepareStatement(sql);
    if (query.getTimeout() > 0) {
      pstmt.setQueryTimeout(query.getTimeout());
    }
    if (query.getBufferFetchSizeHint() > 0) {
      pstmt.setFetchSize(query.getBufferFetchSizeHint());
    }

    BindParams bindParams = query.getBindParams();
    if (!bindParams.isEmpty()) {
      this.bindLog = binder.bind(bindParams, pstmt, conn);
    }

    if (isLogSql()) {
      String logSql = TrimLogSql.trim(sql);
      if (TransactionManager.SQL_LOGGER.isTraceEnabled()) {
        logSql = Str.add(logSql, "; --bind(", bindLog, ")");
      }
      trans.logSql(logSql);
    }

    setResultSet(pstmt.executeQuery());

  }

  /**
   * Return the SQL executed for this query.
   */
  public String getSql() {
    return sql;
  }

  /**
   * Return the rows read.
   */
  public int getRowCount() {
    return rowCount - 1;
  }
}
