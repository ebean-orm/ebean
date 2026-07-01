package io.ebeaninternal.server.query;

import io.ebean.QueryIterator;
import io.ebeaninternal.api.SpiQuery;
import io.ebeaninternal.server.core.DtoQueryRequest;
import io.ebeaninternal.server.persist.Binder;
import jakarta.persistence.PersistenceException;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public final class DtoQueryEngine {

  private final Binder binder;
  private final int defaultFetchSizeFindEach;
  private final int defaultFetchSizeFindList;
  private final boolean autoCommitFalseOnFindIterate;

  public DtoQueryEngine(Binder binder, int defaultFetchSizeFindEach, int defaultFetchSizeFindList, boolean autoCommitFalseOnFindIterate) {
    this.binder = binder;
    this.defaultFetchSizeFindEach = defaultFetchSizeFindEach;
    this.defaultFetchSizeFindList = defaultFetchSizeFindList;
    this.autoCommitFalseOnFindIterate = autoCommitFalseOnFindIterate;
  }

  public <T> List<T> findList(DtoQueryRequest<T> request) {
    try {
      if (defaultFetchSizeFindList > 0) {
        request.setDefaultFetchBuffer(defaultFetchSizeFindList);
      }
      request.executeSql(binder, SpiQuery.Type.LIST);
      List<T> rows = new ArrayList<>();
      while (request.next()) {
        rows.add(request.readNextBean());
      }
      return rows;

    } catch (SQLException e) {
      throw new PersistenceException(errMsg(e.getMessage(), request.getSql()), e);
    } finally {
      request.close();
    }
  }

  private <T> void prepareForIterate(DtoQueryRequest<T> request) throws SQLException {
    if (defaultFetchSizeFindEach > 0) {
      request.setDefaultFetchBuffer(defaultFetchSizeFindEach);
    }
    if (autoCommitFalseOnFindIterate) {
      request.setAutoCommitOnFindIterate();
    }
    request.executeSql(binder, SpiQuery.Type.ITERATE);
  }

  public <T> QueryIterator<T> findIterate(DtoQueryRequest<T> request) {
    try {
      prepareForIterate(request);
      return new DtoQueryIterator<>(request);
    } catch (SQLException e) {
      throw new PersistenceException(errMsg(e.getMessage(), request.getSql()), e);
    }
  }

  public <T> void findEach(DtoQueryRequest<T> request, Consumer<T> consumer) {
    try {
      prepareForIterate(request);
      while (request.next()) {
        consumer.accept(request.readNextBean());
      }
    } catch (SQLException e) {
      throw new PersistenceException(errMsg(e.getMessage(), request.getSql()), e);
    } finally {
      request.close();
    }
  }

  public <T> void findEach(DtoQueryRequest<T> request, int batchSize, Consumer<List<T>> consumer) {
    try {
      prepareForIterate(request);
      List<T> buffer = new ArrayList<>();
      while (request.next()) {
        buffer.add(request.readNextBean());
        if (buffer.size() >= batchSize) {
          consumer.accept(buffer);
          buffer.clear();
        }
      }
      if (!buffer.isEmpty()) {
        // consume the remainder
        consumer.accept(buffer);
      }
    } catch (Exception e) {
      throw new PersistenceException(errMsg(e.getMessage(), request.getSql()), e);
    } finally {
      request.close();
    }
  }

  public <T> void findEachWhile(DtoQueryRequest<T> request, Predicate<T> consumer) {
    try {
      prepareForIterate(request);
      while (request.next()) {
        if (!consumer.test(request.readNextBean())) {
          break;
        }
      }
    } catch (SQLException e) {
      throw new PersistenceException(errMsg(e.getMessage(), request.getSql()), e);
    } finally {
      request.close();
    }
  }

  private String errMsg(String msg, String sql) {
    return "Query threw SQLException:" + msg + " Query was:" + sql;
  }
}
