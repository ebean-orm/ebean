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
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * ToOne bean load context.
 */
class DLoadBeanContext extends DLoadBaseContext implements LoadBeanContext {

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
    String path = fullPath + "." + many.getName();
    parent.register(path, many, collection);
  }

  /**
   * Reset the buffers after a query iterator reset.
   */
  public void clear() {
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

  protected void register(EntityBeanIntercept ebi) {
    if (currentBuffer.isFull()) {
      currentBuffer = createBuffer(batchSize);
    }
    ebi.setBeanLoader(currentBuffer, getPersistenceContext());
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
          if (!loadBuffer.list.isEmpty()) {
            parent.getEbeanServer().loadBean(new LoadBeanRequest(loadBuffer, parentRequest));
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
    private final List<EntityBeanIntercept> list;
    private PersistenceContext persistenceContext;

    LoadBuffer(DLoadBeanContext context, int batchSize) {
      this.context = context;
      this.batchSize = batchSize;
      this.list = new ArrayList<>(batchSize);
    }

    @Override
    public Lock lock() {
      bufferLock.lock();
      return bufferLock;
    }

    @Override
    public int getBatchSize() {
      return batchSize;
    }

    /**
     * Return true if the buffer is full.
     */
    public boolean isFull() {
      return batchSize == list.size();
    }

    /**
     * Add the bean to the load buffer.
     */
    public void add(EntityBeanIntercept ebi) {
      if (persistenceContext == null) {
        // get persistenceContext from first loaded bean into the buffer
        persistenceContext = ebi.getPersistenceContext();
      }
      list.add(ebi);
    }

    @Override
    public List<EntityBeanIntercept> getBatch() {
      return list;
    }

    @Override
    public String getName() {
      return context.serverName;
    }

    @Override
    public String getFullPath() {
      return context.fullPath;
    }

    @Override
    public BeanDescriptor<?> getBeanDescriptor() {
      return context.desc;
    }

    @Override
    public PersistenceContext getPersistenceContext() {
      return persistenceContext;
    }

    @Override
    public void configureQuery(SpiQuery<?> query, String lazyLoadProperty) {
      context.configureQuery(query, lazyLoadProperty);
    }

    @Override
    public void loadBean(EntityBeanIntercept ebi) {
      // A lock is effectively held by EntityBeanIntercept.loadBean()
      if (context.desc.lazyLoadMany(ebi, context)) {
        // lazy load property was a Many
        return;
      }
      if (list.isEmpty()) {
        // re-add to the batch and lazy load from DB skipping l2 cache
        list.add(ebi);
      } else if (context.hitCache) {
        Set<EntityBeanIntercept> hits = context.desc.cacheBeanLoadAll(list, persistenceContext, ebi.getLazyLoadPropertyIndex(), ebi.getLazyLoadProperty());
        list.removeAll(hits);
        if (list.isEmpty() || hits.contains(ebi)) {
          // successfully hit the L2 cache so don't invoke DB lazy loading
          return;
        }
      }

      LoadBeanRequest req = new LoadBeanRequest(this, ebi, context.hitCache);
      context.desc.getEbeanServer().loadBean(req);
      list.clear();
    }
  }

}
