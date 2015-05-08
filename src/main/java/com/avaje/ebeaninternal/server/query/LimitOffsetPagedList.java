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
 * @param <T>
 *          the entity bean type
 */
public class LimitOffsetPagedList<T> implements PagedList<T> {

  private final transient EbeanServer server;

  private final SpiQuery<T> query;

  private final int pageSize;

  private final int pageIndex;

  private final Monitor monitor = new Monitor();

  private int foregroundTotalRowCount = -1;

  private Future<Integer> futureRowCount;

  private List<T> list;

  public LimitOffsetPagedList(EbeanServer server, SpiQuery<T> query, int pageIndex, int pageSize) {
    this.server = server;
    this.query = query;
    this.pageSize = pageSize;
    this.pageIndex = pageIndex;
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
        query.setFirstRow(pageIndex * pageSize);
        query.setMaxRows(pageSize);
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
      return ((rowCount - 1) / pageSize) + 1;
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
    return pageIndex < (getTotalPageCount() - 1);
  }

  public boolean hasPrev() {
    return pageIndex > 0;
  }

  public int getPageIndex() {
    return pageIndex;
  }

  public int getPageSize() {
    return pageSize;
  }

  public String getDisplayXtoYofZ(String to, String of) {

    int first = pageIndex * pageSize + 1;
    int last = first + getList().size() - 1;
    int total = getTotalRowCount();

    return first + to + last + of + total;
  }

}
