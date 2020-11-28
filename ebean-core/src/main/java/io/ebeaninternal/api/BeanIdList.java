package io.ebeaninternal.api;

import java.util.List;

/**
 * Wrapper of the list of Id's.
 */
public class BeanIdList {

  private final List<Object> idList;

  private boolean hasMore;

  public BeanIdList(List<Object> idList) {
    this.idList = idList;
  }

  /**
   * Add an Id to the list.
   */
  public void add(Object id) {
    idList.add(id);
  }

  /**
   * Return the list of Id's.
   */
  public List<Object> getIdList() {
    return idList;
  }

  /**
   * Return true if max rows was hit and there is more rows to fetch.
   */
  public boolean isHasMore() {
    return hasMore;
  }

  /**
   * Set to true when max rows is hit and there are more rows to fetch.
   */
  public void setHasMore(boolean hasMore) {
    this.hasMore = hasMore;
  }

}
