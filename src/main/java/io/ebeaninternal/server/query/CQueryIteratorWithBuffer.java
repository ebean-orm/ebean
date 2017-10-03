package io.ebeaninternal.server.query;

import io.ebean.QueryIterator;
import io.ebeaninternal.server.core.OrmQueryRequest;

import javax.persistence.PersistenceException;
import java.sql.SQLException;
import java.util.ArrayList;

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
    this.buffer = new ArrayList<>(bufferSize);
  }

  @Override
  @SuppressWarnings("unchecked")
  public boolean hasNext() {
    try {
      if (buffer.isEmpty() && moreToLoad) {
        // load buffer
        request.flushPersistenceContextOnIterate();

        int i = -1;
        while (moreToLoad && ++i < bufferSize) {
          if (cquery.hasNext()) {
            buffer.add((T) cquery.next());
          } else {
            moreToLoad = false;
          }
        }
        request.executeSecondaryQueries(true);
      }
      return !buffer.isEmpty();

    } catch (SQLException e) {
      throw cquery.createPersistenceException(e);
    }
  }

  @Override
  public T next() {
    return buffer.remove(0);
  }

  @Override
  public void close() {
    cquery.updateExecutionStatisticsIterator();
    cquery.close();
    request.endTransIfRequired();
  }

  @Override
  public void remove() {
    throw new PersistenceException("Remove not allowed");
  }
}
