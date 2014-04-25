package com.avaje.ebeaninternal.server.cache;

import java.util.List;

/**
 * The cached data for O2M and M2M relationships.
 * <p>
 * This is effectively just the Id values for each of the beans in the collection.
 * </p>
 */
public class CachedManyIds {

  private final List<Object> idList;

  public CachedManyIds(List<Object> idList) {
    this.idList = idList;
  }

  public String toString() {
    return idList.toString();
  }
  
  public List<Object> getIdList() {
    return idList;
  }

}
