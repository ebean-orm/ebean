package io.ebeaninternal.server.loadcontext;

import io.ebean.FetchConfig;
import io.ebean.bean.ObjectGraphNode;
import io.ebean.bean.PersistenceContext;
import io.ebeaninternal.api.SpiQuery;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.querydefn.OrmQueryProperties;

/**
 * Base class for Bean and BeanCollection loading (lazy loading and query join loading).
 */
public abstract class DLoadBaseContext {

  protected final DLoadContext parent;

  protected final BeanDescriptor<?> desc;

  protected final String fullPath;

  protected final OrmQueryProperties queryProps;

  protected final boolean hitCache;

  protected final String serverName;

  protected final int firstBatchSize;

  protected final int secondaryBatchSize;

  protected final ObjectGraphNode objectGraphNode;

  protected final boolean queryFetch;


  public DLoadBaseContext(DLoadContext parent, BeanDescriptor<?> desc, String path, int defaultBatchSize, OrmQueryProperties queryProps) {

    this.parent = parent;
    this.serverName = parent.getEbeanServer().getName();
    this.desc = desc;
    this.queryProps = queryProps;
    this.fullPath = parent.getFullPath(path);
    this.hitCache = parent.isBeanCacheGet() && desc.isBeanCaching();
    this.objectGraphNode = parent.getObjectGraphNode(path);

    this.queryFetch = queryProps != null && queryProps.isQueryFetch();
    this.firstBatchSize = initFirstBatchSize(defaultBatchSize, queryProps);
    this.secondaryBatchSize = initSecondaryBatchSize(defaultBatchSize, firstBatchSize, queryProps);
  }

  private int initFirstBatchSize(int batchSize, OrmQueryProperties queryProps) {
    if (queryProps == null) {
      return batchSize;
    }

    int queryBatchSize = queryProps.getQueryFetchBatch();
    if (queryBatchSize == -1) {
      return batchSize;

    } else if (queryBatchSize == 0) {
      return 100;

    } else {
      return queryBatchSize;
    }
  }

  private int initSecondaryBatchSize(int defaultBatchSize, int firstBatchSize, OrmQueryProperties queryProps) {
    if (queryProps == null) {
      return defaultBatchSize;
    }
    FetchConfig fetchConfig = queryProps.getFetchConfig();
    if (fetchConfig.isQueryAll()) {
      return firstBatchSize;
    }

    int lazyBatchSize = fetchConfig.getLazyBatchSize();
    return (lazyBatchSize > 1) ? lazyBatchSize : defaultBatchSize;
  }

  /**
   * If the parent has a query plan label then extend it with the path and
   * set onto the secondary query.
   */
  void setLabel(SpiQuery<?> query) {

    String label = parent.getPlanLabel();
    if (label != null) {
      label += "_" + fullPath;
      query.setLabel(label);
      query.setProfileLocation(parent.getProfileLocation());
    }
  }

  protected PersistenceContext getPersistenceContext() {
    return parent.getPersistenceContext();
  }

}
