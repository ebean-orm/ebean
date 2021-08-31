package io.ebeaninternal.server.query;

import io.ebean.QueryIterator;
import io.ebeaninternal.server.core.DtoQueryRequest;

import javax.persistence.PersistenceException;
import java.sql.SQLException;

final class DtoQueryIterator<T> implements QueryIterator<T> {

  private final DtoQueryRequest<T> request;
  private boolean closed;

  DtoQueryIterator(DtoQueryRequest<T> request) {
    this.request = request;
  }

  @Override
  public boolean hasNext() {
    boolean result = false;
    try {
      result = request.next();
      return result;
    } catch (SQLException e) {
      throw new PersistenceException(e);
    } finally {
      if (!result) {
        close();
      }
    }
  }

  @Override
  public T next() {
    try {
      return request.readNextBean();
    } catch (SQLException e) {
      throw new PersistenceException(e);
    }
  }

  @Override
  public void close() {
    if (!closed) {
      closed = true;
      request.close();
      request.endTransIfRequired();
    }
  }

}
