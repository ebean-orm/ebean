package io.ebeaninternal.server.query.dto;

import io.ebeaninternal.api.SpiQuery;
import io.ebeaninternal.server.core.DtoQueryRequest;
import io.ebeaninternal.server.core.Message;
import io.ebeaninternal.server.persist.Binder;

import javax.persistence.PersistenceException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class DtoQueryEngine {

  private final Binder binder;

  public DtoQueryEngine(Binder binder) {
    this.binder = binder;
  }

  public <T> List<T> findList(DtoQueryRequest<T> request) {
    try {
      request.executeSql(binder, SpiQuery.Type.LIST);
      List<T> rows = new ArrayList<>();
      while (request.next()) {
        rows.add(request.readNextBean());
      }
      return rows;

    } catch (Throwable e) {
      throw new PersistenceException(Message.msg("fetch.error", e.getMessage(), request.getSql()), e);
    } finally {
      request.close();
    }
  }

  public <T> void findEach(DtoQueryRequest<T> request, Consumer<T> consumer) {
     try {
      request.executeSql(binder, SpiQuery.Type.ITERATE);
      while (request.next()) {
        consumer.accept(request.readNextBean());
      }
    } catch (Exception e) {
      throw new PersistenceException(Message.msg("fetch.error", e.getMessage(), request.getSql()), e);

    } finally {
      request.close();
    }
  }

  public <T> void findEachWhile(DtoQueryRequest<T> request, Predicate<T> consumer) {
    try {
      request.executeSql(binder, SpiQuery.Type.ITERATE);
      while (request.next()) {
        if (!consumer.test(request.readNextBean())) {
          break;
        }
      }
    } catch (Exception e) {
      throw new PersistenceException(Message.msg("fetch.error", e.getMessage(), request.getSql()), e);

    } finally {
      request.close();
    }
  }
}
