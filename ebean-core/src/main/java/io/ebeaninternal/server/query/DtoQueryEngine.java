package io.ebeaninternal.server.query;

import io.ebean.QueryIterator;
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

  public DtoQueryEngine(Binder binder, int defaultFetchSizeFindEach, int defaultFetchSizeFindList) {
    this.binder = binder;
    this.defaultFetchSizeFindEach = defaultFetchSizeFindEach;
    this.defaultFetchSizeFindList = defaultFetchSizeFindList;
  }

  public <T> List<T> findList(DtoQueryRequest<T> request) {
    try {
      if (defaultFetchSizeFindList > 0) {
        request.setDefaultFetchBuffer(defaultFetchSizeFindList);
      }
      request.executeSql(binder);
      List<T> rows = new ArrayList<>();
      while (request.next()) {
        rows.add(request.readNextBean());
      }
      request.putToQueryCache(rows);
      return rows;

    } catch (SQLException e) {
      throw new PersistenceException(errMsg(e.getMessage(), request.getSql()), e);
    } finally {
      request.close();
    }
  }

  public <T> QueryIterator<T> findIterate(DtoQueryRequest<T> request) {
    try {
      if (defaultFetchSizeFindEach > 0) {
        request.setDefaultFetchBuffer(defaultFetchSizeFindEach);
      }
      request.executeSql(binder);
      return new DtoQueryIterator<>(request);
    } catch (SQLException e) {
      throw new PersistenceException(errMsg(e.getMessage(), request.getSql()), e);
    }
  }

  public <T> void findEach(DtoQueryRequest<T> request, Consumer<T> consumer) {
      if (defaultFetchSizeFindEach > 0) {
        request.setDefaultFetchBuffer(defaultFetchSizeFindEach);
      }
      try {
        request.executeSql(binder);
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
      List<T> buffer = new ArrayList<>();
      if (defaultFetchSizeFindEach > 0) {
        request.setDefaultFetchBuffer(defaultFetchSizeFindEach);
      }
      request.executeSql(binder);
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
      if (defaultFetchSizeFindEach > 0) {
        request.setDefaultFetchBuffer(defaultFetchSizeFindEach);
      }
      request.executeSql(binder);
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
