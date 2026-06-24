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

  protected final String path;

  protected final String serverName;

  final OrmQueryProperties queryProps;

  final boolean hitCache;

  final int batchSize;

  final ObjectGraphNode objectGraphNode;

  final boolean queryFetch;

  DLoadBaseContext(DLoadContext parent, BeanDescriptor<?> desc, String path, OrmQueryProperties queryProps) {
    this.parent = parent;
    this.serverName = parent.server().name();
    this.desc = desc;
    this.queryProps = queryProps;
    this.path = path;
    this.fullPath = parent.fullPath(path);
    this.hitCache = parent.isBeanCacheGet() && desc.isBeanCaching();
    this.objectGraphNode = parent.objectGraphNode(path);
    this.queryFetch = queryProps != null && queryProps.isQueryFetch();
    this.batchSize = parent.batchSize(queryProps);
  }

  /**
   * If the parent has a query plan label then extend it with the path and
   * set onto the secondary query.
   */
  void setLabel(SpiQuery<?> query) {
    String prefix = parent.namePrefix();
    if (prefix != null) {
      query.setProfilePath(prefix, pathMode(query), parent.profileLocation());
    }
  }

  /**
   * Extend the parent query's full name with this load's immediate path segment
   * and load mode. The parent name already carries everything up to this point,
   * so only the immediate path (not the root relative fullPath) is appended to
   * avoid duplicating intermediate path segments on nested secondary loads.
   */
  private String pathMode(SpiQuery<?> query) {
    final var loadMode = query.loadMode();
    return path == null ? loadMode : path + '.' + loadMode;
  }

  PersistenceContext persistenceContext() {
    return parent.persistenceContext();
  }

}
