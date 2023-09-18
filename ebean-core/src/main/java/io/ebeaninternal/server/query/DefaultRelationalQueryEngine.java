package io.ebeaninternal.server.query;

import io.ebean.RowConsumer;
import io.ebean.RowMapper;
import io.ebean.SqlRow;
import io.ebean.core.type.DataReader;
import io.ebean.core.type.ScalarType;
import io.ebean.meta.MetricVisitor;
import io.ebean.metric.MetricFactory;
import io.ebean.metric.TimedMetricMap;
import io.ebeaninternal.server.core.RelationalQueryEngine;
import io.ebeaninternal.server.core.RelationalQueryRequest;
import io.ebeaninternal.server.core.RowReader;
import io.ebeaninternal.server.persist.Binder;

import jakarta.persistence.PersistenceException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Perform native sql fetches.
 */
public final class DefaultRelationalQueryEngine implements RelationalQueryEngine {

  private final Binder binder;
  private final String dbTrueValue;
  private final boolean binaryOptimizedUUID;
  private final TimedMetricMap timedMetricMap;

  public DefaultRelationalQueryEngine(Binder binder, String dbTrueValue, boolean binaryOptimizedUUID) {
    this.binder = binder;
    this.dbTrueValue = dbTrueValue == null ? "true" : dbTrueValue;
    this.binaryOptimizedUUID = binaryOptimizedUUID;
    this.timedMetricMap = MetricFactory.get().createTimedMetricMap("sql.query.");
  }

  @Override
  public void collect(String label, long exeMicros) {
    timedMetricMap.add(label, exeMicros);
  }

  @Override
  public void visitMetrics(MetricVisitor visitor) {
    timedMetricMap.visit(visitor);
  }

  @Override
  public SqlRow createSqlRow(int estimateCapacity) {
    return new DefaultSqlRow(estimateCapacity, 0.75f, dbTrueValue, binaryOptimizedUUID);
  }

  private String errMsg(String msg, String sql) {
    return "Query threw SQLException:" + msg + " Query was:" + sql;
  }

  @Override
  public void findEach(RelationalQueryRequest request, RowConsumer consumer) {
    try {
      request.executeSql(binder);
      request.mapEach(consumer);
      request.logSummary();

    } catch (Exception e) {
      throw new PersistenceException(errMsg(e.getMessage(), request.getSql()), e);

    } finally {
      request.close();
    }
  }

  @Override
  public <T> void findEach(RelationalQueryRequest request, RowReader<T> reader, Predicate<T> consumer) {
    try {
      request.executeSql(binder);
      while (request.next()) {
        if (!consumer.test(reader.read())) {
          break;
        }
      }
      request.logSummary();

    } catch (Exception e) {
      throw new PersistenceException(errMsg(e.getMessage(), request.getSql()), e);

    } finally {
      request.close();
    }
  }

  @Override
  public <T> T findOne(RelationalQueryRequest request, RowMapper<T> mapper) {
    try {
      request.executeSql(binder);
      T value = request.mapOne(mapper);
      request.logSummary();
      return value;

    } catch (Exception e) {
      throw new PersistenceException(errMsg(e.getMessage(), request.getSql()), e);

    } finally {
      request.close();
    }
  }

  @Override
  public <T> List<T> findList(RelationalQueryRequest request, RowReader<T> reader) {
    try {
      request.executeSql(binder);
      List<T> rows = new ArrayList<>();
      while (request.next()) {
        rows.add(reader.read());
      }
      request.logSummary();
      return rows;
    } catch (Exception e) {
      throw new PersistenceException(errMsg(e.getMessage(), request.getSql()), e);

    } finally {
      request.close();
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T findSingleAttribute(RelationalQueryRequest request, Class<T> cls) {
    ScalarType<T> scalarType = (ScalarType<T>) binder.getScalarType(cls);
    try {
      request.executeSql(binder);
      final DataReader dataReader = binder.createDataReader(request.resultSet());
      T value = null;
      if (dataReader.next()) {
        value = scalarType.read(dataReader);
      }
      request.logSummary();
      return value;

    } catch (Exception e) {
      throw new PersistenceException(errMsg(e.getMessage(), request.getSql()), e);

    } finally {
      request.close();
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> List<T> findSingleAttributeList(RelationalQueryRequest request, Class<T> cls) {
    ScalarType<T> scalarType = (ScalarType<T>) binder.getScalarType(cls);
    try {
      request.executeSql(binder);
      final DataReader dataReader = binder.createDataReader(request.resultSet());
      List<T> rows = new ArrayList<>();
      while (dataReader.next()) {
        rows.add(scalarType.read(dataReader));
      }
      request.logSummary();
      return rows;

    } catch (Exception e) {
      throw new PersistenceException(errMsg(e.getMessage(), request.getSql()), e);

    } finally {
      request.close();
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> void findSingleAttributeEach(RelationalQueryRequest request, Class<T> cls, Consumer<T> consumer) {
    ScalarType<T> scalarType = (ScalarType<T>) binder.getScalarType(cls);
    try {
      request.executeSql(binder);
      final DataReader dataReader = binder.createDataReader(request.resultSet());
      while (dataReader.next()) {
        consumer.accept(scalarType.read(dataReader));
      }
      request.logSummary();

    } catch (Exception e) {
      throw new PersistenceException(errMsg(e.getMessage(), request.getSql()), e);

    } finally {
      request.close();
    }
  }
}
