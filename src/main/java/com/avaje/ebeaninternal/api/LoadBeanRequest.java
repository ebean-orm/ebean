package com.avaje.ebeaninternal.api;

import java.util.List;

import com.avaje.ebean.Transaction;
import com.avaje.ebean.bean.EntityBeanIntercept;

/**
 * Request for loading ManyToOne and OneToOne relationships.
 */
public class LoadBeanRequest extends LoadRequest {

  private final List<EntityBeanIntercept> batch;

  private final LoadBeanBuffer LoadBuffer;

  private final String lazyLoadProperty;

  private final boolean loadCache;

  public LoadBeanRequest(LoadBeanBuffer LoadBuffer, Transaction transaction, boolean lazy, String lazyLoadProperty,
      boolean loadCache) {

    super(transaction, lazy);
    this.LoadBuffer = LoadBuffer;
    this.batch = LoadBuffer.getBatch();
    this.lazyLoadProperty = lazyLoadProperty;
    this.loadCache = loadCache;
  }

  public boolean isLoadCache() {
    return loadCache;
  }

  public String getDescription() {
    return "path:" + LoadBuffer.getFullPath() + " batch:" + batch.size();
  }

  /**
   * Return the batch of beans to actually load.
   */
  public List<EntityBeanIntercept> getBatch() {
    return batch;
  }

  /**
   * Return the load context.
   */
  public LoadBeanBuffer getLoadContext() {
    return LoadBuffer;
  }

  /**
   * Return the property that invoked the lazy loading.
   */
  public String getLazyLoadProperty() {
    return lazyLoadProperty;
  }

}
