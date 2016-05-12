package com.avaje.ebeaninternal.server.query;

import com.avaje.ebean.QueryEachConsumer;
import com.avaje.ebean.QueryEachWhileConsumer;
import com.avaje.ebean.SqlRow;
import com.avaje.ebeaninternal.server.core.Message;
import com.avaje.ebeaninternal.server.core.RelationalQueryEngine;
import com.avaje.ebeaninternal.server.core.RelationalQueryRequest;
import com.avaje.ebeaninternal.server.persist.Binder;

import javax.persistence.PersistenceException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Perform native sql fetches.
 */
public class DefaultRelationalQueryEngine implements RelationalQueryEngine {

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
      request.executeSql(binder);

      List<SqlRow> rows = new ArrayList<SqlRow>();
      while (request.next()) {
        rows.add(readRow(request));
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
  }

  /**
   * Read the row from the ResultSet and return as a MapBean.
   */
  protected SqlRow readRow(RelationalQueryRequest request) throws SQLException {
    return request.createNewRow(dbTrueValue);
  }

}
