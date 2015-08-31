package com.avaje.ebeaninternal.server.loadcontext;

import com.avaje.ebean.bean.BeanCollection;
import com.avaje.ebean.bean.BeanCollectionLoader;
import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebean.bean.ObjectGraphNode;
import com.avaje.ebean.bean.PersistenceContext;
import com.avaje.ebeaninternal.api.LoadManyBuffer;
import com.avaje.ebeaninternal.api.LoadManyContext;
import com.avaje.ebeaninternal.api.LoadManyRequest;
import com.avaje.ebeaninternal.api.SpiQuery;
import com.avaje.ebeaninternal.server.core.OrmQueryRequest;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.deploy.BeanPropertyAssocMany;
import com.avaje.ebeaninternal.server.querydefn.OrmQueryProperties;

import java.util.ArrayList;
import java.util.List;

public class DLoadManyContext extends DLoadBaseContext implements LoadManyContext {

  protected final BeanPropertyAssocMany<?> property;

  private List<LoadBuffer> bufferList;

  private LoadBuffer currentBuffer;

  public DLoadManyContext(DLoadContext parent, BeanPropertyAssocMany<?> property,
                          String path, int defaultBatchSize, OrmQueryProperties queryProps) {

    super(parent, property.getBeanDescriptor(), path, defaultBatchSize, queryProps);

    this.property = property;
    // bufferList only required when using query joins (queryFetch)
    this.bufferList = (!queryFetch) ? null : new ArrayList<DLoadManyContext.LoadBuffer>();
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

    // propagate the readOnly state
    if (parent.isReadOnly() != null) {
      query.setReadOnly(parent.isReadOnly());
    }

    // propagate the asOf and lazy loading mode
    query.setDisableLazyLoading(parent.isDisableLazyLoading());
    query.asOf(parent.getAsOf());
    query.setParentNode(objectGraphNode);

    if (queryProps != null) {
      queryProps.configureBeanQuery(query);
    }

    if (parent.isUseAutoTune()) {
      query.setAutofetch(true);
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

  public void loadSecondaryQuery(OrmQueryRequest<?> parentRequest) {

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

        // this is only run once - secondary query is a one shot deal
        this.bufferList = null;
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
      this.list = new ArrayList<BeanCollection<?>>(batchSize);
    }

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

    public void loadMany(BeanCollection<?> bc, boolean onlyIds) {

      synchronized (this) {
        boolean useCache = context.hitCache && !onlyIds;
        if (useCache) {
          EntityBean ownerBean = bc.getOwnerBean();
          BeanDescriptor<?> parentDesc = context.desc.getBeanDescriptor(ownerBean.getClass());
          Object parentId = parentDesc.getId(ownerBean);
          if (parentDesc.cacheManyPropLoad(context.property, bc, parentId, context.parent.isReadOnly())) {
            // we loaded the bean from cache
            list.remove(bc);
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
