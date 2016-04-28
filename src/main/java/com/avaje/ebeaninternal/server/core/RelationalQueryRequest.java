package com.avaje.ebeaninternal.server.core;

import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.QueryEachConsumer;
import com.avaje.ebean.QueryEachWhileConsumer;
import com.avaje.ebean.SqlQuery;
import com.avaje.ebean.SqlRow;
import com.avaje.ebean.Transaction;
import com.avaje.ebeaninternal.api.BindParams;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.ebeaninternal.api.SpiQuery;
import com.avaje.ebeaninternal.api.SpiSqlQuery;
import com.avaje.ebeaninternal.api.SpiTransaction;
import com.avaje.ebeaninternal.server.lib.util.Str;
import com.avaje.ebeaninternal.server.persist.Binder;
import com.avaje.ebeaninternal.server.query.DefaultSqlRow;
import com.avaje.ebeaninternal.server.transaction.TransactionManager;
import com.avaje.ebeaninternal.server.util.BindParamsParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Wraps the objects involved in executing a SqlQuery.
 */
public final class RelationalQueryRequest {

  private static final int GLOBAL_ROW_LIMIT = Integer.valueOf(System.getProperty("ebean.query.globallimit", "1000000"));

  private static final Logger logger = LoggerFactory.getLogger(RelationalQueryRequest.class);

  private final SpiSqlQuery query;

  private final RelationalQueryEngine queryEngine;

  private final SpiEbeanServer ebeanServer;

  private SpiTransaction trans;

  private boolean createdTransaction;

  private SpiQuery.Type queryType;

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
  public RelationalQueryRequest(SpiEbeanServer server, RelationalQueryEngine engine, SqlQuery q, Transaction t) {
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
      trans = ebeanServer.getCurrentServerTransaction();
      if (trans == null || !trans.isActive()) {
        // create a local readOnly transaction
        trans = ebeanServer.createServerTransaction(false, -1);
        createdTransaction = true;
      }
    }
  }

  /**
   * End the transaction if it was locally created.
   */
  public void endTransIfRequired() {
    if (createdTransaction) {
      trans.commit();
    }
  }

  public void findEach(QueryEachConsumer<SqlRow> consumer) {
    queryType = SpiQuery.Type.ITERATE;
    queryEngine.findEach(this, consumer);
  }

  public void findEachWhile(QueryEachWhileConsumer<SqlRow> consumer) {
    queryType = SpiQuery.Type.ITERATE;
    queryEngine.findEach(this, consumer);
  }

  public List<SqlRow> findList() {
    queryType = SpiQuery.Type.LIST;
    return queryEngine.findList(this);
  }

  /**
   * Return the find that is to be performed.
   */
  public SpiSqlQuery getQuery() {
    return query;
  }

  /**
   * Return the type (List, Set or Map) that this fetch returns.
   */
  public SpiQuery.Type getQueryType() {
    return queryType;
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

    ArrayList<String> propNames = new ArrayList<String>();
    ResultSetMetaData metaData = resultSet.getMetaData();

    int columnsPlusOne = metaData.getColumnCount() + 1;
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
    for (int i = 0; i < propertyNames.length; i++) {
      index++;
      Object value = resultSet.getObject(index);
      sqlRow.set(propertyNames[i], value);
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
    this.sql = sql;
  }

  /**
   * Prepare and execute the SQL using the Binder.
   */
  public boolean executeSql(Binder binder) throws SQLException {

    prepareSql();
    synchronized (query) {
      if (query.isCancelled()) {
        logger.trace("Query already cancelled");
        return false;
      }

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
        this.bindLog = binder.bind(bindParams, pstmt);
      }

      if (isLogSql()) {
        String logSql = sql;
        if (TransactionManager.SQL_LOGGER.isTraceEnabled()) {
          logSql = Str.add(logSql, "; --bind(", bindLog, ")");
        }
        trans.logSql(logSql);
      }

      setResultSet(pstmt.executeQuery());
      return true;
    }
  }

  /**
   * Return true if the query has been cancelled.
   */
  public boolean isCancelled() {
    return query.isCancelled();
  }

  /**
   * Return the maxRows allowed to fetch.
   */
  public int getMaxRows() {
    return query.getMaxRows() >= 1 ? query.getMaxRows() : GLOBAL_ROW_LIMIT;
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
