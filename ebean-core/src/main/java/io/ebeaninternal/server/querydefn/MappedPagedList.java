package io.ebeaninternal.server.querydefn;

import io.ebean.DtoMapper;
import io.ebean.PagedList;

import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.locks.ReentrantLock;

/**
 * {@link PagedList} adapter backing {@code query.mapTo(dtoType).findPagedList()}.
 * <p>
 * Delegates all page metadata (total count, page index, {@code hasNext()}/{@code hasPrev()}, ...)
 * straight through to the underlying entity-typed {@link PagedList} unchanged - paging is a
 * property of the query, not of the DTO shape. Only {@link #getList()} differs: it maps the
 * delegate's entity list to the target DTO list (once, cached) via the given {@link DtoMapper}.
 */
public final class MappedPagedList<T, D> implements PagedList<D> {

  private final ReentrantLock lock = new ReentrantLock();
  private final PagedList<T> delegate;
  private final DtoMapper<T, D> mapper;
  private List<D> mappedList;

  public MappedPagedList(PagedList<T> delegate, DtoMapper<T, D> mapper) {
    this.delegate = delegate;
    this.mapper = mapper;
  }

  @Override
  public void loadCount() {
    delegate.loadCount();
  }

  @Override
  public Future<Integer> getFutureCount() {
    return delegate.getFutureCount();
  }

  @Override
  public List<D> getList() {
    lock.lock();
    try {
      if (mappedList == null) {
        mappedList = mapper.mapList(delegate.getList());
      }
      return mappedList;
    } finally {
      lock.unlock();
    }
  }

  @Override
  public int getTotalCount() {
    return delegate.getTotalCount();
  }

  @Override
  public int getTotalPageCount() {
    return delegate.getTotalPageCount();
  }

  @Override
  public int getPageSize() {
    return delegate.getPageSize();
  }

  @Override
  public int getPageIndex() {
    return delegate.getPageIndex();
  }

  @Override
  public boolean hasNext() {
    return delegate.hasNext();
  }

  @Override
  public boolean hasPrev() {
    return delegate.hasPrev();
  }

  @Override
  public String getDisplayXtoYofZ(String to, String of) {
    return delegate.getDisplayXtoYofZ(to, of);
  }
}
