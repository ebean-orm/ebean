package com.avaje.ebeaninternal.server.query;

import java.util.List;
import java.util.concurrent.Future;

import javax.persistence.PersistenceException;

import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.PagedList;
import com.avaje.ebeaninternal.api.Monitor;
import com.avaje.ebeaninternal.api.SpiQuery;

/**
 * PagedList implementation based on limit offset types of queries.
 *
 * @param <T> the entity bean type
 */
public class LimitOffsetPagedList<T> implements PagedList<T> {

  private final transient EbeanServer server;

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
  public LimitOffsetPagedList(EbeanServer server, SpiQuery<T> query) {
    this.server = server;
    this.query = query;
    this.maxRows = query.getMaxRows();
    this.firstRow = query.getFirstRow();
  }

  public void loadCount() {
    getFutureCount();
  }

  public void loadRowCount() {
    loadCount();
  }

  public Future<Integer> getFutureCount() {
    synchronized (monitor) {
      if (futureRowCount == null) {
        futureRowCount = server.findFutureCount(query, null);
      }
      return futureRowCount;
    }
  }

  public Future<Integer> getFutureRowCount() {
    return getFutureCount();
  }

  public List<T> getList() {
    synchronized (monitor) {
      if (list == null) {
        list = server.findList(query, null);
      }
      return list;
    }
  }

  public int getTotalPageCount() {

    int rowCount = getTotalCount();
    if (rowCount == 0) {
      return 0;
    } else {
      return ((rowCount - 1) / maxRows) + 1;
    }
  }

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

  public int getTotalRowCount() {
    return getTotalCount();
  }

  public boolean hasNext() {
    return (firstRow + maxRows) < getTotalCount();
  }

  public boolean hasPrev() {
    return firstRow > 0;
  }

  public int getPageSize() {
    return maxRows;
  }

  public String getDisplayXtoYofZ(String to, String of) {

    int first = firstRow + 1;
    int last = firstRow + getList().size();
    int total = getTotalCount();

    return first + to + last + of + total;
  }

}
