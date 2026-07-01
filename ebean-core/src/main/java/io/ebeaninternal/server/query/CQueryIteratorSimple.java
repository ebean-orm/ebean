package io.ebeaninternal.server.query;

import io.ebean.QueryIterator;
import io.ebeaninternal.server.core.OrmQueryRequest;

import java.sql.SQLException;

/**
 * QueryIterator that does not require a buffer for secondary queries.
 */
final class CQueryIteratorSimple<T> implements QueryIterator<T> {

  private final CQuery<T> cquery;
  private final OrmQueryRequest<T> request;
  private boolean closed;

  CQueryIteratorSimple(CQuery<T> cquery, OrmQueryRequest<T> request) {
    this.cquery = cquery;
    this.request = request;
  }

  @Override
  public boolean hasNext() {
    boolean ret = false;
    try {
      ret = cquery.hasNext();
      return ret;
    } catch (SQLException e) {
      throw cquery.createPersistenceException(e);
    } finally {
      if (!ret) {
        close();
      }
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public T next() {
    return (T) cquery.nextBean();
  }

  @Override
  public void close() {
    if (!closed) {
      closed = true;
      cquery.updateExecutionStatisticsIterator();
      cquery.close();
      request.endTransIfRequired();
    }
  }

}
