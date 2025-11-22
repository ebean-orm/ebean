package io.ebeaninternal.server.query;

import io.ebean.RowConsumer;
import io.ebean.RowMapper;
import io.ebean.SqlRow;
import io.ebean.core.type.DataReader;
import io.ebean.core.type.ScalarType;
import io.ebean.meta.MetricVisitor;
import io.ebean.metric.MetricFactory;
import io.ebean.metric.TimedMetricMap;
import io.ebeaninternal.api.SpiQuery;
import io.ebeaninternal.server.core.RelationalQueryEngine;
import io.ebeaninternal.server.core.RelationalQueryRequest;
import io.ebeaninternal.server.core.RowReader;
import io.ebeaninternal.server.persist.Binder;

import jakarta.persistence.NonUniqueResultException;
import jakarta.persistence.PersistenceException;

import java.sql.SQLException;
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
  private final boolean autoCommitFalseOnFindIterate;
  private final TimedMetricMap timedMetricMap;
  private final int defaultFetchSizeFindEach;
  private final int defaultFetchSizeFindList;

  public DefaultRelationalQueryEngine(Binder binder, String dbTrueValue, boolean binaryOptimizedUUID,
                                      int defaultFetchSizeFindEach, int defaultFetchSizeFindList,
                                      boolean autoCommitFalseOnFindIterate) {
    this.binder = binder;
    this.dbTrueValue = dbTrueValue == null ? "true" : dbTrueValue;
    this.binaryOptimizedUUID = binaryOptimizedUUID;
    this.autoCommitFalseOnFindIterate = autoCommitFalseOnFindIterate;
    this.timedMetricMap = MetricFactory.get().createTimedMetricMap("sql.query.");
    this.defaultFetchSizeFindEach = defaultFetchSizeFindEach;
    this.defaultFetchSizeFindList = defaultFetchSizeFindList;
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

  private <T> void prepareForIterate(RelationalQueryRequest request) throws SQLException {
    if (defaultFetchSizeFindEach > 0) {
      request.setDefaultFetchBuffer(defaultFetchSizeFindEach);
    }
    if (autoCommitFalseOnFindIterate) {
      request.setAutoCommitOnFindIterate();
    }
    request.executeSql(binder, SpiQuery.Type.ITERATE);
  }

  @Override
  public void findEach(RelationalQueryRequest request, RowConsumer consumer) {
    try {
      prepareForIterate(request);
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
      prepareForIterate(request);
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
      request.executeSql(binder, SpiQuery.Type.BEAN);
      T value = request.mapOne(mapper);
      if (request.next()) {
        throw new NonUniqueResultException("Got more than 1 result for findOne");
      }
      request.logSummary();
      return value;

    } catch (NonUniqueResultException e) {
      throw e;
    } catch (Exception e) {
      throw new PersistenceException(errMsg(e.getMessage(), request.getSql()), e);

    } finally {
      request.close();
    }
  }

  @Override
  public <T> List<T> findList(RelationalQueryRequest request, RowReader<T> reader) {
    try {
      if (defaultFetchSizeFindList > 0) {
        request.setDefaultFetchBuffer(defaultFetchSizeFindList);
      }
      request.executeSql(binder, SpiQuery.Type.LIST);
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
      if (defaultFetchSizeFindList > 0) {
        request.setDefaultFetchBuffer(defaultFetchSizeFindList);
      }
      request.executeSql(binder, SpiQuery.Type.ATTRIBUTE);
      final DataReader dataReader = binder.createDataReader(false, request.resultSet());
      T value = null;
      if (dataReader.next()) {
        value = scalarType.read(dataReader);
        if (dataReader.next()) {
          throw new NonUniqueResultException("Got more than 1 result for findSingleAttribute");
        }
      }
      request.logSummary();
      return value;

    } catch (NonUniqueResultException e) {
      throw e;
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
      if (defaultFetchSizeFindList > 0) {
        request.setDefaultFetchBuffer(defaultFetchSizeFindList);
      }
      request.executeSql(binder, SpiQuery.Type.ATTRIBUTE);
      final DataReader dataReader = binder.createDataReader(false, request.resultSet());
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
      if (defaultFetchSizeFindEach > 0) {
        request.setDefaultFetchBuffer(defaultFetchSizeFindEach);
      }
      request.executeSql(binder, SpiQuery.Type.ATTRIBUTE);
      final DataReader dataReader = binder.createDataReader(false, request.resultSet());
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
