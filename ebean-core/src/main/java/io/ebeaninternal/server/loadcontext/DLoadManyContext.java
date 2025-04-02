package io.ebeaninternal.server.loadcontext;

import io.ebean.bean.*;
import io.ebeaninternal.api.LoadManyBuffer;
import io.ebeaninternal.api.LoadManyContext;
import io.ebeaninternal.api.LoadManyRequest;
import io.ebeaninternal.api.SpiQuery;
import io.ebeaninternal.server.core.OrmQueryRequest;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.deploy.BeanPropertyAssocMany;
import io.ebeaninternal.server.querydefn.OrmQueryProperties;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * ToMany bean load context.
 */
final class DLoadManyContext extends DLoadBaseContext implements LoadManyContext {

  private final BeanPropertyAssocMany<?> property;
  private List<LoadBuffer> bufferList;
  private LoadBuffer currentBuffer;

  DLoadManyContext(DLoadContext parent, BeanPropertyAssocMany<?> property, String path, OrmQueryProperties queryProps) {
    super(parent, property.descriptor(), path, queryProps);
    this.property = property;
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
  private void clear() {
    if (bufferList != null) {
      bufferList.clear();
    }
    currentBuffer = createBuffer(batchSize);
  }

  private void configureQuery(SpiQuery<?> query) {
    setLabel(query);
    parent.propagateQueryState(query);
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
    return parent.server().name();
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
          if (loadBuffer.size() > 0) {
            LoadManyRequest req = new LoadManyRequest(loadBuffer, parentRequest);
            parent.server().loadMany(req);
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
  static final class LoadBuffer implements BeanCollectionLoader, LoadManyBuffer {

    private final ReentrantLock lock = new ReentrantLock();
    private final PersistenceContext persistenceContext;
    private final DLoadManyContext context;
    private final int batchSize;
    private final BeanCollection<?>[] list;
    private int size;

    LoadBuffer(DLoadManyContext context, int batchSize) {
      this.context = context;
      // set the persistence context as at this moment in
      // case it changes as part of a findIterate etc
      this.persistenceContext = context.persistenceContext();
      this.batchSize = batchSize;
      this.list = new BeanCollection<?>[batchSize];
    }

    @Override
    public int batchSize() {
      return batchSize;
    }

    /**
     * Return true if the buffer is full.
     */
    public boolean isFull() {
      return batchSize() == size();
    }

    @Override
    public BeanPropertyAssocMany<?> beanProperty() {
      return context.property;
    }

    @Override
    public ObjectGraphNode objectGraphNode() {
      return context.objectGraphNode;
    }

    @Override
    public void configureQuery(SpiQuery<?> query) {
      context.configureQuery(query);
    }

    @Override
    public String name() {
      return context.serverName;
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
    public String fullPath() {
      return context.fullPath;
    }

    public void add(BeanCollection<?> bc) {
      list[size++] = bc;
    }

    void clear() {
      Arrays.fill(list, null);
      size = 0;
    }

    @Override
    public int size() {
      return size;
    }

    @Override
    public BeanCollection<?> get(int i) {
      return list[i];
    }

    @Override
    public boolean removeFromBuffer(BeanCollection<?> collection) {
      for (int i = 0; i < size; i++) {
        // find it using instance equality - avoiding equals() and potential deadlock issue
        if (list[i] == collection) {
          list[i] = null;
          return true;
        }
      }
      return false;
    }

    @Override
    public void loadMany(BeanCollection<?> bc, boolean onlyIds) {
      lock.lock();
      try {
        boolean useCache = !onlyIds && context.hitCache && context.property.isUseCache();
        if (useCache) {
          EntityBean ownerBean = bc.owner();
          BeanDescriptor<?> parentDesc = context.desc.descriptor(ownerBean.getClass());
          Object parentId = parentDesc.getId(ownerBean);
          final String parentKey = parentDesc.cacheKey(parentId);
          if (parentDesc.cacheManyPropLoad(context.property, bc, parentKey)) {
            // we loaded the bean collection from cache so remove it from the buffer
            if (removeFromBuffer(bc)) {
              bc.setLoader(context.parent.server());
            }
            // find it using instance equality - avoiding equals() and potential deadlock issue
            return;
          }
        }

        context.parent.server().loadMany(new LoadManyRequest(this, onlyIds, useCache, bc));
        // clear the buffer as all entries have been loaded
        clear();
      } finally {
        lock.unlock();
      }
    }
  }
}
