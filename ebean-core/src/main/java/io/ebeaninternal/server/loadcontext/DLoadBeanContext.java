package io.ebeaninternal.server.loadcontext;

import io.ebean.CacheMode;
import io.ebean.bean.BeanCollection;
import io.ebean.bean.BeanLoader;
import io.ebean.bean.EntityBeanIntercept;
import io.ebean.bean.PersistenceContext;
import io.ebeaninternal.api.LoadBeanBuffer;
import io.ebeaninternal.api.LoadBeanContext;
import io.ebeaninternal.api.LoadBeanRequest;
import io.ebeaninternal.api.SpiQuery;
import io.ebeaninternal.server.core.OrmQueryRequest;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.deploy.BeanPropertyAssocMany;
import io.ebeaninternal.server.querydefn.OrmQueryProperties;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * ToOne bean load context.
 */
final class DLoadBeanContext extends DLoadBaseContext implements LoadBeanContext {

  private final boolean cache;
  private List<LoadBuffer> bufferList;
  private LoadBuffer currentBuffer;

  DLoadBeanContext(DLoadContext parent, BeanDescriptor<?> desc, String path, OrmQueryProperties queryProps) {
    super(parent, desc, path, queryProps);
    // bufferList only required when using query joins (queryFetch)
    this.bufferList = (!queryFetch) ? null : new ArrayList<>();
    this.currentBuffer = createBuffer(batchSize);
    this.cache = (queryProps != null) && queryProps.isCache();
  }

  @Override
  public void register(BeanPropertyAssocMany<?> many, BeanCollection<?> collection) {
    String path = fullPath + "." + many.name();
    parent.register(path, many, collection);
  }

  /**
   * Reset the buffers after a query iterator reset.
   */
  private void clear() {
    if (bufferList != null) {
      bufferList.clear();
    }
    currentBuffer = createBuffer(batchSize);
  }

  private void configureQuery(SpiQuery<?> query, String lazyLoadProperty) {
    if (cache) {
      query.setBeanCacheMode(CacheMode.ON);
    }
    setLabel(query);
    parent.propagateQueryState(query, desc.isDocStoreMapped());
    query.setParentNode(objectGraphNode);
    query.setLazyLoadProperty(lazyLoadProperty);
    if (queryProps != null) {
      queryProps.configureBeanQuery(query);
    }
  }

  void register(EntityBeanIntercept ebi) {
    if (currentBuffer.isFull()) {
      currentBuffer = createBuffer(batchSize);
    }
    ebi.setBeanLoader(currentBuffer, persistenceContext());
    currentBuffer.add(ebi);
  }

  private LoadBuffer createBuffer(int size) {
    LoadBuffer buffer = new LoadBuffer(this, size);
    if (bufferList != null) {
      bufferList.add(buffer);
    }
    return buffer;
  }

  @Override
  public void loadSecondaryQuery(OrmQueryRequest<?> parentRequest, boolean forEach) {
    if (!queryFetch) {
      throw new IllegalStateException("Not expecting loadSecondaryQuery() to be called?");
    }
    lock.lock();
    try {
      if (bufferList != null) {
        for (LoadBuffer loadBuffer : bufferList) {
          if (!loadBuffer.batch.isEmpty()) {
            parent.server().loadBean(new LoadBeanRequest(loadBuffer, parentRequest));
          }
          if (forEach) {
            clear();
          } else {
            // this is only run once - secondary query is a one shot deal
            this.bufferList = null;
          }
        }
      }
    } finally {
      lock.unlock();
    }
  }

  /**
   * A buffer for batch loading beans on a given path.
   */
  static class LoadBuffer implements BeanLoader, LoadBeanBuffer {

    private final ReentrantLock bufferLock = new ReentrantLock();
    private final DLoadBeanContext context;
    private final int batchSize;
    private final Set<EntityBeanIntercept> batch;
    private PersistenceContext persistenceContext;

    LoadBuffer(DLoadBeanContext context, int batchSize) {
      this.context = context;
      this.batchSize = batchSize;
      this.batch = new HashSet<>(Math.max((int) (batchSize/.75f) + 1, 16));
    }

    @Override
    public String toString() {
      return "LoadBuffer@" + hashCode();
    }

    @Override
    public Lock lock() {
      bufferLock.lock();
      return bufferLock;
    }

    @Override
    public int batchSize() {
      return batchSize;
    }

    /**
     * Return true if the buffer is full.
     */
    public boolean isFull() {
      return batchSize == batch.size();
    }

    /**
     * Add the bean to the load buffer.
     */
    public void add(EntityBeanIntercept ebi) {
      if (persistenceContext == null) {
        // get persistenceContext from first loaded bean into the buffer
        persistenceContext = ebi.persistenceContext();
      }
      batch.add(ebi);
    }

    @Override
    public Set<EntityBeanIntercept> batch() {
      return batch;
    }

    @Override
    public String name() {
      return context.serverName;
    }

    @Override
    public String fullPath() {
      return context.fullPath;
    }

    @Override
    public BeanDescriptor<?> descriptor() {
      return context.desc;
    }

    @Override
    public PersistenceContext persistenceContext() {
      return persistenceContext;
    }

    @Override
    public void configureQuery(SpiQuery<?> query, String lazyLoadProperty) {
      context.configureQuery(query, lazyLoadProperty);
    }

    @Override
    public boolean isCache() {
      return context.cache;
    }

    @Override
    public void loadBean(EntityBeanIntercept ebi) {
      // A lock is effectively held by EntityBeanIntercept.loadBean()
      if (context.desc.lazyLoadMany(ebi, context)) {
        // lazy load property was a Many
        return;
      }
      if (!batch.contains(ebi)) {
        // re-add to the batch and lazy load from DB skipping l2 cache
        batch.add(ebi);
      } else if (context.hitCache) {
        Set<EntityBeanIntercept> hits = context.desc.cacheBeanLoadAll(batch, persistenceContext, ebi.lazyLoadPropertyIndex(), ebi.lazyLoadProperty());
        batch.removeAll(hits);
        if (batch.isEmpty() || hits.contains(ebi)) {
          // successfully hit the L2 cache so don't invoke DB lazy loading
          return;
        }
      }

      LoadBeanRequest req = new LoadBeanRequest(this, ebi, context.hitCache);
      context.desc.ebeanServer().loadBean(req);
      batch.clear();
    }
  }

}
