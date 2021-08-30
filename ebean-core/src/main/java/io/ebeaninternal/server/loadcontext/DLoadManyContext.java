package io.ebeaninternal.server.loadcontext;

import io.ebean.bean.BeanCollection;
import io.ebean.bean.BeanCollectionLoader;
import io.ebean.bean.EntityBean;
import io.ebean.bean.ObjectGraphNode;
import io.ebean.bean.PersistenceContext;
import io.ebeaninternal.api.LoadManyBuffer;
import io.ebeaninternal.api.LoadManyContext;
import io.ebeaninternal.api.LoadManyRequest;
import io.ebeaninternal.api.SpiQuery;
import io.ebeaninternal.server.core.OrmQueryRequest;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.deploy.BeanPropertyAssocMany;
import io.ebeaninternal.server.querydefn.OrmQueryProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * ToMany bean load context.
 */
final class DLoadManyContext extends DLoadBaseContext implements LoadManyContext {

  private final BeanPropertyAssocMany<?> property;
  private final boolean docStoreMapped;
  private List<LoadBuffer> bufferList;
  private LoadBuffer currentBuffer;

  DLoadManyContext(DLoadContext parent, BeanPropertyAssocMany<?> property, String path, OrmQueryProperties queryProps) {
    super(parent, property.getBeanDescriptor(), path, queryProps);
    this.property = property;
    this.docStoreMapped = property.isTargetDocStoreMapped();
    // bufferList only required when using query joins (queryFetch)
    this.bufferList = (!queryFetch) ? null : new ArrayList<>();
    this.currentBuffer = createBuffer(batchSize);
  }

  private LoadBuffer createBuffer(int size) {
    LoadBuffer buffer = new LoadBuffer(this, size);
    if (bufferList != null) {
      bufferList.add(buffer);
    }
    return buffer;
  }

  /**
   * Reset the buffers for a query iterator reset.
   */
  public void clear() {
    if (bufferList != null) {
      bufferList.clear();
    }
    currentBuffer = createBuffer(batchSize);
  }

  private void configureQuery(SpiQuery<?> query) {
    setLabel(query);
    parent.propagateQueryState(query, docStoreMapped);
    query.setParentNode(objectGraphNode);
    if (queryProps != null) {
      queryProps.configureBeanQuery(query);
    }
  }

  public BeanPropertyAssocMany<?> getBeanProperty() {
    return property;
  }

  public BeanDescriptor<?> getBeanDescriptor() {
    return desc;
  }


  public String getName() {
    return parent.getEbeanServer().getName();
  }

  public void register(BeanCollection<?> bc) {
    if (currentBuffer.isFull()) {
      currentBuffer = createBuffer(batchSize);
    }
    currentBuffer.add(bc);
    bc.setLoader(currentBuffer);
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
            LoadManyRequest req = new LoadManyRequest(loadBuffer, parentRequest);
            parent.getEbeanServer().loadMany(req);
          }
        }
        if (forEach) {
          clear();
        } else {
          // this is only run once - secondary query is a one shot deal
          this.bufferList = null;
        }
      }
    } finally {
      lock.unlock();
    }
  }

  /**
   * A buffer for batch loading bean collections on a given path.
   * Supports batch lazy loading and secondary query loading.
   */
  static class LoadBuffer implements BeanCollectionLoader, LoadManyBuffer {

    private final ReentrantLock lock = new ReentrantLock();
    private final PersistenceContext persistenceContext;
    private final DLoadManyContext context;
    private final int batchSize;
    private final List<BeanCollection<?>> list;

    LoadBuffer(DLoadManyContext context, int batchSize) {
      this.context = context;
      // set the persistence context as at this moment in
      // case it changes as part of a findIterate etc
      this.persistenceContext = context.getPersistenceContext();
      this.batchSize = batchSize;
      this.list = new ArrayList<>(batchSize);
    }

    @Override
    public boolean isUseDocStore() {
      return context.parent.useDocStore && context.docStoreMapped;
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
     * Return true if the buffer is full.
     */
    public void add(BeanCollection<?> bc) {
      list.add(bc);
    }

    @Override
    public List<BeanCollection<?>> getBatch() {
      return list;
    }

    @Override
    public BeanPropertyAssocMany<?> getBeanProperty() {
      return context.property;
    }

    @Override
    public ObjectGraphNode getObjectGraphNode() {
      return context.objectGraphNode;
    }

    @Override
    public void configureQuery(SpiQuery<?> query) {
      context.configureQuery(query);
    }

    @Override
    public String getName() {
      return context.serverName;
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
    public String getFullPath() {
      return context.fullPath;
    }

    @Override
    public void loadMany(BeanCollection<?> bc, boolean onlyIds) {
      lock.lock();
      try {
        boolean useCache = !onlyIds && context.hitCache && context.property.isUseCache();
        if (useCache) {
          EntityBean ownerBean = bc.getOwnerBean();
          BeanDescriptor<?> parentDesc = context.desc.getBeanDescriptor(ownerBean.getClass());
          Object parentId = parentDesc.getId(ownerBean);
          if (parentDesc.cacheManyPropLoad(context.property, bc, parentId, context.parent.isReadOnly())) {
            // we loaded the bean collection from cache so remove it from the buffer
            for (int i = 0; i < list.size(); i++) {
              // find it using instance equality - avoiding equals() and potential deadlock issue
              if (list.get(i) == bc) {
                list.remove(i);
                bc.setLoader(context.parent.getEbeanServer());
                return;
              }
            }
            return;
          }
        }

        context.parent.getEbeanServer().loadMany(new LoadManyRequest(this, onlyIds, useCache));
        // clear the buffer as all entries have been loaded
        list.clear();
      } finally {
        lock.unlock();
      }
    }

  }
}
