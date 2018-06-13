package io.ebeaninternal.server.query;

import io.ebean.PagedList;
import io.ebeaninternal.api.Monitor;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.api.SpiQuery;

import javax.persistence.PersistenceException;
import java.util.List;
import java.util.concurrent.Future;

/**
 * PagedList implementation based on limit offset types of queries.
 *
 * @param <T> the entity bean type
 */
public class LimitOffsetPagedList<T> implements PagedList<T> {

  private final transient SpiEbeanServer server;

  private final SpiQuery<T> query;

  private final int firstRow;

  private final int maxRows;

  private final Monitor monitor = new Monitor();

  private int foregroundTotalRowCount = -1;

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
    synchronized (monitor) {
      if (futureRowCount == null) {
        futureRowCount = server.findFutureCount(query, null);
      }
      return futureRowCount;
    }
  }

  @Override
  public List<T> getList() {
    synchronized (monitor) {
      if (list == null) {
        list = server.findList(query, null);
      }
      return list;
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
    synchronized (monitor) {
      if (futureRowCount != null) {
        try {
          // background query already initiated so get it with a wait
          return futureRowCount.get();
        } catch (Exception e) {
          throw new PersistenceException(e);
        }
      }
      // already fetched?
      if (foregroundTotalRowCount > -1) return foregroundTotalRowCount;

      // just using foreground thread
      foregroundTotalRowCount = server.findCount(query, null);
      return foregroundTotalRowCount;
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
