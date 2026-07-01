package io.ebeaninternal.server.query;

import io.ebean.QueryIterator;
import io.ebeaninternal.server.core.OrmQueryRequest;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.NoSuchElementException;

/**
 * A QueryIterator that uses a buffer to execute secondary queries periodically.
 */
final class CQueryIteratorWithBuffer<T> implements QueryIterator<T> {

  private final CQuery<T> cquery;
  private final int bufferSize;
  private final OrmQueryRequest<T> request;
  private final ArrayList<T> buffer;

  private boolean closed;
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
    boolean ret = false;
    try {
      if (buffer.isEmpty() && moreToLoad) {
        // load buffer
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
      request.unmodifiableFreeze(buffer);
      ret = !buffer.isEmpty();
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
  public T next() {
    if (buffer.isEmpty()) {
      throw new NoSuchElementException();
    }
    return buffer.remove(0);
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
