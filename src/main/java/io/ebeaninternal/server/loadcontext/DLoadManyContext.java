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

public class DLoadManyContext extends DLoadBaseContext implements LoadManyContext {

  protected final BeanPropertyAssocMany<?> property;

  private final boolean docStoreMapped;

  private List<LoadBuffer> bufferList;

  private LoadBuffer currentBuffer;

  public DLoadManyContext(DLoadContext parent, BeanPropertyAssocMany<?> property,
                          String path, int defaultBatchSize, OrmQueryProperties queryProps) {

    super(parent, property.getBeanDescriptor(), path, defaultBatchSize, queryProps);

    this.property = property;
    this.docStoreMapped = property.isTargetDocStoreMapped();
    // bufferList only required when using query joins (queryFetch)
    this.bufferList = (!queryFetch) ? null : new ArrayList<>();
    this.currentBuffer = createBuffer(firstBatchSize);
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
    currentBuffer = createBuffer(secondaryBatchSize);
  }

  public void configureQuery(SpiQuery<?> query) {

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
      currentBuffer = createBuffer(secondaryBatchSize);
    }
    currentBuffer.add(bc);
    bc.setLoader(currentBuffer);
  }

  @Override
  public void loadSecondaryQuery(OrmQueryRequest<?> parentRequest, boolean forEach) {

    if (!queryFetch) {
      throw new IllegalStateException("Not expecting loadSecondaryQuery() to be called?");
    }
    synchronized (this) {
      if (bufferList != null) {
        for (LoadBuffer loadBuffer : bufferList) {
          if (!loadBuffer.list.isEmpty()) {
            LoadManyRequest req = new LoadManyRequest(loadBuffer, parentRequest);
            parent.getEbeanServer().loadMany(req);
            if (!queryProps.isQueryFetchAll()) {
              // Stop - only fetch the first batch ... the rest will be lazy loaded
              break;
            }
          }
        }

        if (forEach) {
          clear();
        } else {
          // this is only run once - secondary query is a one shot deal
          this.bufferList = null;
        }
      }
    }
  }

  /**
   * A buffer for batch loading bean collections on a given path.
   * Supports batch lazy loading and secondary query loading.
   */
  public static class LoadBuffer implements BeanCollectionLoader, LoadManyBuffer {

    private final PersistenceContext persistenceContext;
    private final DLoadManyContext context;
    private final int batchSize;
    private final List<BeanCollection<?>> list;

    public LoadBuffer(DLoadManyContext context, int batchSize) {
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

      synchronized (this) {
        boolean useCache = context.hitCache && !onlyIds;
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
                return;
              }
            }
            return;
          }
        }

        // Should reduce the list by checking each beanCollection in the L2 first before executing the query

        LoadManyRequest req = new LoadManyRequest(this, onlyIds, useCache);
        context.parent.getEbeanServer().loadMany(req);
      }
    }

  }
}
