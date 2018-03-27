package io.ebeaninternal.server.query;

import io.ebean.SqlRow;
import io.ebean.meta.MetricType;
import io.ebean.meta.MetricVisitor;
import io.ebeaninternal.api.SpiQuery;
import io.ebeaninternal.metric.MetricFactory;
import io.ebeaninternal.metric.TimedMetricMap;
import io.ebeaninternal.server.core.Message;
import io.ebeaninternal.server.core.RelationalQueryEngine;
import io.ebeaninternal.server.core.RelationalQueryRequest;
import io.ebeaninternal.server.persist.Binder;

import javax.persistence.PersistenceException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Perform native sql fetches.
 */
public class DefaultRelationalQueryEngine implements RelationalQueryEngine {

  private final Binder binder;

  private final String dbTrueValue;

  private final boolean binaryOptimizedUUID;

  private final TimedMetricMap timedMetricMap;

  public DefaultRelationalQueryEngine(Binder binder, String dbTrueValue, boolean binaryOptimizedUUID) {
    this.binder = binder;
    this.dbTrueValue = dbTrueValue == null ? "true" : dbTrueValue;
    this.binaryOptimizedUUID = binaryOptimizedUUID;
    this.timedMetricMap = MetricFactory.get().createTimedMetricMap(MetricType.SQL, "sql.query.");
  }

  @Override
  public void collect(String label, long exeMicros, int rows) {
    timedMetricMap.add(label, exeMicros, rows);
  }

  @Override
  public void visitMetrics(MetricVisitor visitor) {
    timedMetricMap.visit(visitor);
  }

  @Override
  public SqlRow createSqlRow(int estimateCapacity) {
    return new DefaultSqlRow(estimateCapacity, 0.75f, dbTrueValue, binaryOptimizedUUID);
  }

  @Override
  public void findEach(RelationalQueryRequest request, Predicate<SqlRow> consumer) {

    try {
      request.executeSql(binder, SpiQuery.Type.ITERATE);
      while (request.next()) {
        if (!consumer.test(readRow(request))) {
          break;
        }
      }
      request.logSummary();

    } catch (Exception e) {
      throw new PersistenceException(Message.msg("fetch.error", e.getMessage(), request.getSql()), e);

    } finally {
      request.close();
    }
  }

  @Override
  public void findEach(RelationalQueryRequest request, Consumer<SqlRow> consumer) {

    try {
      request.executeSql(binder, SpiQuery.Type.ITERATE);
      while (request.next()) {
        consumer.accept(readRow(request));
      }
      request.logSummary();

    } catch (Exception e) {
      throw new PersistenceException(Message.msg("fetch.error", e.getMessage(), request.getSql()), e);

    } finally {
      request.close();
    }
  }

  @Override
  public List<SqlRow> findList(RelationalQueryRequest request) {

    try {
      request.executeSql(binder, SpiQuery.Type.LIST);
      List<SqlRow> rows = new ArrayList<>();
      while (request.next()) {
        rows.add(readRow(request));
      }

      request.logSummary();
      return rows;

    } catch (Exception e) {
      throw new PersistenceException(Message.msg("fetch.error", e.getMessage(), request.getSql()), e);

    } finally {
      request.close();
    }
  }

  /**
   * Read the row from the ResultSet and return as a MapBean.
   */
  private SqlRow readRow(RelationalQueryRequest request) throws SQLException {
    return request.createNewRow();
  }

}
