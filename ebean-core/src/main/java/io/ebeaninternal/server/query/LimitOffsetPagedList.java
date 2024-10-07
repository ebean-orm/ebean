package io.ebeaninternal.server.query;

import io.ebean.PagedList;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.api.SpiQuery;
import jakarta.persistence.PersistenceException;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.locks.ReentrantLock;

/**
 * PagedList implementation based on limit offset types of queries.
 */
public final class LimitOffsetPagedList<T> implements PagedList<T> {

  private final transient SpiEbeanServer server;
  private final transient ReentrantLock lock = new ReentrantLock();
  private final SpiQuery<T> query;
  private final int firstRow;
  private final int maxRows;

  private int totalRowCount = -1;
  private Future<Integer> futureRowCount;
  private List<T> list;

  /**
   * Construct with firstRow/maxRows.
   */
  public LimitOffsetPagedList(SpiEbeanServer server, SpiQuery<T> query) {
    this.server = server;
    this.query = query;
    this.maxRows = query.getMaxRows();
    this.firstRow = query.getFirstRow();
  }

  @Override
  public void loadCount() {
    getFutureCount();
  }

  @Override
  public Future<Integer> getFutureCount() {
    lock.lock();
    try {
      if (futureRowCount == null) {
        futureRowCount = server.findFutureCount(query);
      }
      return futureRowCount;
    } finally {
      lock.unlock();
    }
  }

  @Override
  public List<T> getList() {
    lock.lock();
    try {
      if (list == null) {
        if (totalRowCount == 0) {
          // already count and no rows
          list = Collections.emptyList();
        } else {
          list = server.findList(query);
        }
      }
      return list;
    } finally {
      lock.unlock();
    }
  }

  @Override
  public int getPageIndex() {
    if (firstRow == 0) {
      return 0;
    }
    return ((firstRow - 1) / maxRows) + 1;
  }

  @Override
  public int getTotalPageCount() {
    int rowCount = getTotalCount();
    if (rowCount == 0) {
      return 0;
    } else {
      return ((rowCount - 1) / maxRows) + 1;
    }
  }

  @Override
  public int getTotalCount() {
    lock.lock();
    try {
      if (totalRowCount > -1) {
        return totalRowCount;
      }
      if (futureRowCount != null) {
        try {
          // background query already initiated so get it with a wait
          totalRowCount = futureRowCount.get();
          return totalRowCount;
        } catch (Exception e) {
          throw new PersistenceException(e);
        }
      }
      // just using foreground thread
      totalRowCount = server.findCount(query);
      return totalRowCount;
    } finally {
      lock.unlock();
    }
  }

  @Override
  public boolean hasNext() {
    return (firstRow + maxRows) < getTotalCount();
  }

  @Override
  public boolean hasPrev() {
    return firstRow > 0;
  }

  @Override
  public int getPageSize() {
    return maxRows;
  }

  @Override
  public String getDisplayXtoYofZ(String to, String of) {
    int first = firstRow + 1;
    int last = firstRow + getList().size();
    int total = getTotalCount();
    return first + to + last + of + total;
  }

}
