package io.ebean;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;

/**
 * An empty PagedList.
 * <p>
 * For use in application code when we need to return a PagedList but don't want to
 * execute a query.
 * </p>
 *
 * <pre>{@code
 *
 *   PagedList<Customer> empty = PagedList.emptyList();
 *
 * }</pre>
 */
public class EmptyPagedList<T> implements PagedList<T> {

  @Override
  public void loadCount() {
    // do nothing
  }

  @Nonnull
  @Override
  public Future<Integer> getFutureCount() {
    return null;
  }

  @Nonnull
  @Override
  public List<T> getList() {
    return Collections.emptyList();
  }

  @Override
  public int getTotalCount() {
    return 0;
  }

  @Override
  public int getTotalPageCount() {
    return 0;
  }

  @Override
  public int getPageSize() {
    return 0;
  }

  @Override
  public int getPageIndex() {
    return 0;
  }

  @Override
  public boolean hasNext() {
    return false;
  }

  @Override
  public boolean hasPrev() {
    return false;
  }

  @Override
  public String getDisplayXtoYofZ(String to, String of) {
    return "";
  }
}
