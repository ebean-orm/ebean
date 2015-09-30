package com.avaje.ebeaninternal.server.query;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.PersistenceException;

import com.avaje.ebean.QueryIterator;
import com.avaje.ebeaninternal.server.core.OrmQueryRequest;
import com.avaje.ebeaninternal.server.transaction.DefaultPersistenceContext;

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
      HashMap<String, DefaultPersistenceContext.ClassContext> typeCache =
              ((DefaultPersistenceContext)request.getPersistenceContext()).getTypeCache();
      request.flushPersistenceContextOnIterate();
      for (Map.Entry<String, DefaultPersistenceContext.ClassContext> entry : typeCache.entrySet()){
        entry.getValue().resetMapWithLastInserted();
      }
      ((DefaultPersistenceContext) request.getPersistenceContext()).setTypeCache(typeCache);
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