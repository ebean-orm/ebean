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

  public DtoQueryEngine(Binder binder) {
    this.binder = binder;
  }

  public <T> List<T> findList(DtoQueryRequest<T> request) {
    try {
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
      request.executeSql(binder);
      return new DtoQueryIterator<>(request);
    } catch (SQLException e) {
      throw new PersistenceException(errMsg(e.getMessage(), request.getSql()), e);
    }
  }

  public <T> void findEach(DtoQueryRequest<T> request, Consumer<T> consumer) {
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
