package com.avaje.ebeaninternal.server.query;

import com.avaje.ebean.QueryEachConsumer;
import com.avaje.ebean.QueryEachWhileConsumer;
import com.avaje.ebean.SqlQueryListener;
import com.avaje.ebean.SqlRow;
import com.avaje.ebeaninternal.api.SpiSqlQuery;
import com.avaje.ebeaninternal.server.core.Message;
import com.avaje.ebeaninternal.server.core.RelationalQueryEngine;
import com.avaje.ebeaninternal.server.core.RelationalQueryRequest;
import com.avaje.ebeaninternal.server.persist.Binder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.PersistenceException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Perform native sql fetches.
 */
public class DefaultRelationalQueryEngine implements RelationalQueryEngine {

  private static final Logger logger = LoggerFactory.getLogger(DefaultRelationalQueryEngine.class);


  private final Binder binder;

  private final String dbTrueValue;

  public DefaultRelationalQueryEngine(Binder binder, String dbTrueValue) {
    this.binder = binder;
    this.dbTrueValue = dbTrueValue == null ? "true" : dbTrueValue;
  }

  @Override
  public void findEach(RelationalQueryRequest request, QueryEachWhileConsumer<SqlRow> consumer) {

    long startTime = System.currentTimeMillis();
    try {
      request.executeSql(binder);
      while (request.next()) {
        if (!consumer.accept(readRow(request))) {
          break;
        }
      }
      logSummary(request, startTime);

    } catch (Exception e) {
      throw new PersistenceException(Message.msg("fetch.error", e.getMessage(), request.getSql()), e);

    } finally {
      request.close();
    }
  }

  @Override
  public void findEach(RelationalQueryRequest request, QueryEachConsumer<SqlRow> consumer) {

    long startTime = System.currentTimeMillis();

    try {
      request.executeSql(binder);
      while (request.next()) {
        consumer.accept(readRow(request));
      }
      logSummary(request, startTime);

    } catch (Exception e) {
      throw new PersistenceException(Message.msg("fetch.error", e.getMessage(), request.getSql()), e);

    } finally {
      request.close();
    }
  }

  public List<SqlRow> findList(RelationalQueryRequest request) {

    long startTime = System.currentTimeMillis();
    try {
      if (!request.executeSql(binder)) {
        return null;
      }

      int maxRows = request.getMaxRows();

      int loadRowCount = 0;

      List<SqlRow> rows = new ArrayList<SqlRow>();

      SpiSqlQuery query = request.getQuery();
      SqlQueryListener listener = query.getListener();

      while (request.next()) {
        SqlRow bean;
        synchronized (query) {
          if (request.isCancelled()) {
            break;
          }
          bean = readRow(request);
        }
        if (bean != null) {
          if (listener != null) {
            listener.process(bean);
          } else {
            rows.add(bean);
          }
          loadRowCount++;
          if (loadRowCount == maxRows) {
            // break, as we have hit the max rows to fetch...
            break;
          }
        }
      }

      logSummary(request, startTime);

      return rows;

    } catch (Exception e) {
      throw new PersistenceException(Message.msg("fetch.error", e.getMessage(), request.getSql()), e);

    } finally {
      request.close();
    }
  }

  private void logSummary(RelationalQueryRequest request, long startTime) {

    if (request.isLogSummary()) {
      long exeTime = System.currentTimeMillis() - startTime;
      request.getTransaction().logSummary("SqlQuery  rows[" + request.getRowCount() + "] time[" + exeTime + "] bind[" + request.getBindLog() + "]");
    }

    if (request.isCancelled()) {
      logger.debug("Query was cancelled during execution rows: {}", request.getRowCount());
    }
  }

  /**
   * Read the row from the ResultSet and return as a MapBean.
   */
  protected SqlRow readRow(RelationalQueryRequest request) throws SQLException {
    return request.createNewRow(dbTrueValue);
  }

}
