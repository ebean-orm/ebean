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

  public void loadRowCount() {
    getFutureRowCount();
  }

  public Future<Integer> getFutureRowCount() {
    synchronized (monitor) {
      if (futureRowCount == null) {
        futureRowCount = server.findFutureRowCount(query, null);
      }
      return futureRowCount;
    }
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

    int rowCount = getTotalRowCount();
    if (rowCount == 0) {
      return 0;
    } else {
      return ((rowCount - 1) / maxRows) + 1;
    }
  }

  public int getTotalRowCount() {
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
      foregroundTotalRowCount = server.findRowCount(query, null);
      return foregroundTotalRowCount;
    }
  }

  public boolean hasNext() {
    return (firstRow + maxRows) < getTotalRowCount();
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
    int total = getTotalRowCount();

    return first + to + last + of + total;
  }

}
