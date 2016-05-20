package com.avaje.ebeaninternal.server.query;

import java.sql.SQLException;

import javax.persistence.PersistenceException;

import com.avaje.ebeaninternal.server.core.QueryIterator;
import com.avaje.ebeaninternal.server.core.OrmQueryRequest;

/**
 * QueryIterator that does not require a buffer for secondary queries.
 */
class CQueryIteratorSimple<T> implements QueryIterator<T> {

  private final CQuery<T> cquery;

  private final OrmQueryRequest<T> request;

  CQueryIteratorSimple(CQuery<T> cquery, OrmQueryRequest<T> request) {
    this.cquery = cquery;
    this.request = request;
  }

  public boolean hasNext() {
    try {
      request.flushPersistenceContextOnIterate();
      return cquery.hasNext();
    } catch (SQLException e) {
      throw cquery.createPersistenceException(e);
    }
  }

  @SuppressWarnings("unchecked")
  public T next() {
    return (T) cquery.next();
  }

  public void close() {
    cquery.updateExecutionStatistics();
    cquery.close();
    request.endTransIfRequired();
  }

  public void remove() {
    throw new PersistenceException("Remove not allowed");
  }
}