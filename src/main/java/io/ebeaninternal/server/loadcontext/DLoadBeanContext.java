package io.ebeaninternal.server.loadcontext;

import io.ebean.bean.BeanLoader;
import io.ebean.bean.EntityBeanIntercept;
import io.ebean.bean.PersistenceContext;
import io.ebeaninternal.api.LoadBeanBuffer;
import io.ebeaninternal.api.LoadBeanContext;
import io.ebeaninternal.api.LoadBeanRequest;
import io.ebeaninternal.api.SpiQuery;
import io.ebeaninternal.server.core.OrmQueryRequest;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.querydefn.OrmQueryProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Default implementation of LoadBeanContext.
 */
public class DLoadBeanContext extends DLoadBaseContext implements LoadBeanContext {

  private List<LoadBuffer> bufferList;

  private LoadBuffer currentBuffer;

  public DLoadBeanContext(DLoadContext parent, BeanDescriptor<?> desc, String path, int defaultBatchSize, OrmQueryProperties queryProps) {
    super(parent, desc, path, defaultBatchSize, queryProps);
    // bufferList only required when using query joins (queryFetch)
    this.bufferList = (!queryFetch) ? null : new ArrayList<>();
    this.currentBuffer = createBuffer(firstBatchSize);
  }

  /**
   * Reset the buffers after a query iterator reset.
   */
  public void clear() {
    if (bufferList != null) {
      bufferList.clear();
    }
    currentBuffer = createBuffer(secondaryBatchSize);
  }

  protected void configureQuery(SpiQuery<?> query, String lazyLoadProperty) {

    parent.propagateQueryState(query, desc.isDocStoreMapped());
    query.setParentNode(objectGraphNode);
    query.setLazyLoadProperty(lazyLoadProperty);
    if (queryProps != null) {
      queryProps.configureBeanQuery(query);
    }
  }

  protected void register(EntityBeanIntercept ebi) {

    if (currentBuffer.isFull()) {
      currentBuffer = createBuffer(secondaryBatchSize);
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
    synchronized (this) {
      if (bufferList != null) {
        for (LoadBuffer loadBuffer : bufferList) {
          if (!loadBuffer.list.isEmpty()) {
            LoadBeanRequest req = new LoadBeanRequest(loadBuffer, parentRequest);
            parent.getEbeanServer().loadBean(req);
            if (!queryProps.isQueryFetchAll()) {
              // Stop - only fetch the first batch ... the rest will be lazy loaded
              break;
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
  }


  /**
   * A buffer for batch loading beans on a given path.
   */
  public static class LoadBuffer implements BeanLoader, LoadBeanBuffer {

    private final DLoadBeanContext context;
    private final int batchSize;
    private final List<EntityBeanIntercept> list;
    private PersistenceContext persistenceContext;

    public LoadBuffer(DLoadBeanContext context, int batchSize) {
      this.context = context;
      this.batchSize = batchSize;
      this.list = new ArrayList<>(batchSize);
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
      // A synchronized (this) is effectively held by EntityBeanIntercept.loadBean()

      if (context.desc.lazyLoadMany(ebi)) {
        // lazy load property was a Many
        return;
      }

      if (context.hitCache && context.desc.cacheBeanLoad(ebi, persistenceContext)) {
        // successfully hit the L2 cache so don't invoke DB lazy loading
        list.remove(ebi);
        return;
      }

      if (context.hitCache) {
        // check each of the beans in the batch to see if they are in the L2 cache.
        // bean successfully loaded from L2 cache so remove from batch load
        list.removeIf(batchEbi -> batchEbi != ebi && context.desc.cacheBeanLoad(batchEbi, persistenceContext));
      }

      LoadBeanRequest req = new LoadBeanRequest(this, ebi.getLazyLoadProperty(), context.hitCache);
      context.desc.getEbeanServer().loadBean(req);
    }

  }

}
