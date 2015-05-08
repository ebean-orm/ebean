package com.avaje.ebeaninternal.server.query;

import java.sql.SQLException;
import java.util.ArrayList;

import javax.persistence.PersistenceException;

import com.avaje.ebean.QueryIterator;
import com.avaje.ebeaninternal.server.core.OrmQueryRequest;

/**
 * A QueryIterator that uses a buffer to execute secondary queries periodically.
 */
class CQueryIteratorWithBuffer<T> implements QueryIterator<T> {

  private final CQuery<T> cquery;
  private final int bufferSize;
  private final OrmQueryRequest<T> request;
  private final ArrayList<T> buffer;

  private boolean moreToLoad = true;

  CQueryIteratorWithBuffer(CQuery<T> cquery, OrmQueryRequest<T> request, int bufferSize) {
    this.cquery = cquery;
    this.request = request;
    this.bufferSize = bufferSize;
    this.buffer = new ArrayList<T>(bufferSize);
  }

  @SuppressWarnings("unchecked")
  public boolean hasNext() {
    try {
      if (buffer.isEmpty() && moreToLoad) {
        // load buffer
        request.flushPersistenceContextOnIterate();

        int i = -1;
        while (moreToLoad && ++i < bufferSize) {
          if (cquery.hasNextBean()) {
            buffer.add((T)cquery.getLoadedBean());
          } else {
            moreToLoad = false;
          }
        }
        // execute secondary queries
        request.executeSecondaryQueries();
      }
      return !buffer.isEmpty();

    } catch (SQLException e) {
      throw cquery.createPersistenceException(e);
    }
  }

  public T next() {
    return buffer.remove(0);
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