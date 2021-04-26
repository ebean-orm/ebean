package io.ebeaninternal.server.loadcontext;

import io.ebean.bean.ObjectGraphNode;
import io.ebean.bean.PersistenceContext;
import io.ebeaninternal.api.SpiQuery;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.querydefn.OrmQueryProperties;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Base class for Bean and BeanCollection loading (lazy loading and query join loading).
 */
abstract class DLoadBaseContext {

  protected final ReentrantLock lock = new ReentrantLock();

  protected final DLoadContext parent;

  protected final BeanDescriptor<?> desc;

  protected final String fullPath;

  protected final String serverName;

  final OrmQueryProperties queryProps;

  final boolean hitCache;

  final int batchSize;

  final ObjectGraphNode objectGraphNode;

  final boolean queryFetch;

  DLoadBaseContext(DLoadContext parent, BeanDescriptor<?> desc, String path, OrmQueryProperties queryProps) {
    this.parent = parent;
    this.serverName = parent.getEbeanServer().getName();
    this.desc = desc;
    this.queryProps = queryProps;
    this.fullPath = parent.getFullPath(path);
    this.hitCache = parent.isBeanCacheGet() && desc.isBeanCaching();
    this.objectGraphNode = parent.getObjectGraphNode(path);
    this.queryFetch = queryProps != null && queryProps.isQueryFetch();
    this.batchSize = parent.batchSize(queryProps);
  }

  /**
   * If the parent has a query plan label then extend it with the path and
   * set onto the secondary query.
   */
  void setLabel(SpiQuery<?> query) {
    String label = parent.getPlanLabel();
    if (label != null) {
      query.setProfilePath(label, fullPath, parent.getProfileLocation());
    }
  }

  PersistenceContext getPersistenceContext() {
    return parent.getPersistenceContext();
  }

}
