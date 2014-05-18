package com.avaje.ebeaninternal.server.loadcontext;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.avaje.ebean.bean.BeanLoader;
import com.avaje.ebean.bean.EntityBeanIntercept;
import com.avaje.ebean.bean.PersistenceContext;
import com.avaje.ebeaninternal.api.LoadBeanBuffer;
import com.avaje.ebeaninternal.api.LoadBeanContext;
import com.avaje.ebeaninternal.api.LoadBeanRequest;
import com.avaje.ebeaninternal.api.SpiQuery;
import com.avaje.ebeaninternal.server.core.OrmQueryRequest;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.querydefn.OrmQueryProperties;

/**
 * Default implementation of LoadBeanContext.
 *
 */
public class DLoadBeanContext extends DLoadBaseContext implements LoadBeanContext{
	
  private List<LoadBuffer> bufferList;
  
  private LoadBuffer currentBuffer;

  public DLoadBeanContext(DLoadContext parent, BeanDescriptor<?> desc, String path, int defaultBatchSize, OrmQueryProperties queryProps) {
    
    super(parent, desc, path, defaultBatchSize, queryProps);
    
    this.bufferList = new ArrayList<DLoadBeanContext.LoadBuffer>();
    this.currentBuffer = createBuffer(firstBatchSize);  
  }
  
  protected void configureQuery(SpiQuery<?> query, String lazyLoadProperty) {

    // propagate the readOnly state
    if (parent.isReadOnly() != null) {
      query.setReadOnly(parent.isReadOnly());
    }
    query.setParentNode(objectGraphNode);
    query.setLazyLoadProperty(lazyLoadProperty);

    if (queryProps != null) {
      queryProps.configureBeanQuery(query);
    }
    if (parent.isUseAutofetchManager()) {
      query.setAutofetch(true);
    }
  }

  protected void register(EntityBeanIntercept ebi){

    ebi.setBeanLoader(0, currentBuffer, getPersistenceContext());
    if (currentBuffer.add(ebi)) {
      // the currentBuffer is full so create another one
      currentBuffer = createBuffer(secondaryBatchSize);
    }
  }
  
	private LoadBuffer createBuffer(int size) {
	  LoadBuffer buffer = new LoadBuffer(this, size);
	  if (bufferList != null) {
	    bufferList.add(buffer);
	  }
    return buffer;
	}
	
  public void loadSecondaryQuery(OrmQueryRequest<?> parentRequest, int requestedBatchSize, boolean all) {

    if (!queryFetch) {
      throw new IllegalStateException("Not expecting loadSecondaryQuery() to be called?");
    }
    synchronized (this) {
      
      if (bufferList != null) {
        for (LoadBuffer loadBuffer : bufferList) {
          if (!loadBuffer.list.isEmpty()) {
            boolean loadCache = false;
            LoadBeanRequest req = new LoadBeanRequest(loadBuffer, parentRequest, false, null, loadCache);
    
            parent.getEbeanServer().loadBean(req);  
            if (!queryProps.isQueryFetchAll()) {
              // Stop - only fetch the first batch ... the rest will be lazy loaded
              break;
            }
          }
          // this is only run once - secondary query is a one shot deal
          this.bufferList = null;
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
    
    public LoadBuffer(DLoadBeanContext context, int batchSize) {
      this.context = context;
      this.batchSize = batchSize;
      this.list = new ArrayList<EntityBeanIntercept>(batchSize);
    }

    /**
     * Return true if the buffer is full.
     */
    public boolean add(EntityBeanIntercept ebi) {
      list.add(ebi);
      return batchSize == list.size();
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
      return context.getPersistenceContext();
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
     
      if (context.hitCache && context.desc.cacheBeanLoad(ebi)) {
        // successfully hit the L2 cache so don't invoke DB lazy loading
        list.remove(ebi);
        return;
      }

      if (context.hitCache) {
        // Check each of the beans in the batch to see if they are in the L2 cache.
        Iterator<EntityBeanIntercept> iterator = list.iterator();
        while (iterator.hasNext()) {
          EntityBeanIntercept bean = iterator.next();
          if (context.desc.cacheBeanLoad(bean)) {
            iterator.remove();
          }
        }
      }

      LoadBeanRequest req = new LoadBeanRequest(this, true, ebi.getLazyLoadProperty(), context.hitCache);
      context.desc.getEbeanServer().loadBean(req);
    }

  }
	
}
