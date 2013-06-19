package com.avaje.ebeaninternal.server.loadcontext;

import java.util.ArrayList;
import java.util.List;

import com.avaje.ebean.bean.BeanLoader;
import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebean.bean.EntityBeanIntercept;
import com.avaje.ebean.bean.ObjectGraphNode;
import com.avaje.ebean.bean.PersistenceContext;
import com.avaje.ebeaninternal.api.LoadBeanContext;
import com.avaje.ebeaninternal.api.LoadBeanRequest;
import com.avaje.ebeaninternal.api.LoadContext;
import com.avaje.ebeaninternal.api.SpiQuery;
import com.avaje.ebeaninternal.server.core.OrmQueryRequest;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.querydefn.OrmQueryProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of LoadBeanContext.
 *
 */
public class DLoadBeanContext implements LoadBeanContext, BeanLoader {

	private static final Logger logger = LoggerFactory.getLogger(DLoadBeanContext.class);
	
	protected final DLoadContext parent;

	protected final BeanDescriptor<?> desc;
	
	protected final String path;
	
	protected final String fullPath;

	private final DLoadList<EntityBeanIntercept> weakList;

	private final OrmQueryProperties queryProps;
	
	private int batchSize;

  public DLoadBeanContext(DLoadContext parent, BeanDescriptor<?> desc, String path, int batchSize, 
      OrmQueryProperties queryProps, DLoadList<EntityBeanIntercept> weakList) {
    
    this.parent = parent;
    this.desc = desc;
    this.path = path;
    this.batchSize = batchSize;
    this.queryProps = queryProps;
    this.weakList = weakList;

    if (parent.getRelativePath() == null) {
      this.fullPath = path;
    } else {
      this.fullPath = parent.getRelativePath() + "." + path;
    }
  }
	
  public void configureQuery(SpiQuery<?> query, String lazyLoadProperty) {

    // propagate the readOnly state
    if (parent.isReadOnly() != null) {
      query.setReadOnly(parent.isReadOnly());
    }
    query.setParentNode(getObjectGraphNode());
    query.setLazyLoadProperty(lazyLoadProperty);

    if (queryProps != null) {
      queryProps.configureBeanQuery(query);
    }
    if (parent.isUseAutofetchManager()) {
      query.setAutofetch(true);
    }
  }

	public String getFullPath() {
		return fullPath;
	}
	
	public PersistenceContext getPersistenceContext() {
		return parent.getPersistenceContext();
	}

	public OrmQueryProperties getQueryProps() {
		return queryProps;
	}

	public ObjectGraphNode getObjectGraphNode() {
		return parent.getObjectGraphNode(path);
	}
	
	public String getPath() {
		return path;
	}
	
	public String getName() {
		return parent.getEbeanServer().getName();
	}

	public int getBatchSize() {
		return batchSize;
	}

	public void setBatchSize(int batchSize) {
		this.batchSize = batchSize;
	}

	public BeanDescriptor<?> getBeanDescriptor() {
		return desc;
	}

	public LoadContext getGraphContext() {
		return parent;
	}

	public void register(EntityBeanIntercept ebi){
		int pos = weakList.add(ebi);
		ebi.setBeanLoader(pos, this, parent.getPersistenceContext());
	}

	/**
	 * Check if we can load the bean from L2 cache. If so avoid loading from the DB.
	 */
	private boolean loadBeanFromCache(EntityBeanIntercept ebi, int position) {
	
	  if (!desc.loadFromCache(ebi)) {
	    return false;
	  }
    // we loaded the bean from cache
    weakList.removeEntry(position);
    if (logger.isTraceEnabled()) {
      logger.trace("Loading path:" + fullPath + " - bean loaded from L2 cache, position[" + position + "]");
    }
    return true;
	}
	
	/**
	 * Load this bean and potentially a batch of similar beans.
	 */
  public void loadBean(EntityBeanIntercept ebi) {

    // A synchronized (this) is effectively held by EntityBeanIntercept.loadBean()

    if (desc.lazyLoadMany(ebi)) {
      // lazy load property was a Many
      return;
    }
   
    int position = ebi.getBeanLoaderIndex();
    boolean hitCache = !parent.isExcludeBeanCache() && desc.isBeanCaching();

    if (hitCache && loadBeanFromCache(ebi, position)) {
      // successfully hit the L2 cache so don't invoke DB lazy loading
      return;
    }

    // Get a batch of beans to lazy load
    List<EntityBeanIntercept> batch = null;
    try {
      batch = weakList.getLoadBatch(position, batchSize);
    } catch (IllegalStateException e) {
      logger.error("type[" + desc.getFullName() + "] fullPath[" + fullPath + "] batchSize[" + batchSize + "]", e);
    }
    
    if (hitCache && batchSize > 1) {
      // Check each of the beans in the batch to see if they are in the L2 cache.
      // Add more as necessary to make up our batch that will be loaded.
      batch = loadBeanCheckBatch(batch);
    }

    if (logger.isTraceEnabled()) {
      for (int i = 0; i < batch.size(); i++) {
        
        EntityBeanIntercept entityBeanIntercept = batch.get(i);
        EntityBean owner = entityBeanIntercept.getOwner();
        Object id = desc.getId(owner);
        
        logger.trace("LoadBean type["+owner.getClass().getName()+"] fullPath["+fullPath+"] id["+id+"] batchIndex["+i+"] beanLoaderIndex["+entityBeanIntercept.getBeanLoaderIndex()+"]");
      }
    }
    
    int lazyLoadIndex = ebi.getLazyLoadProperty();
    String lazyLoadProp = ebi.getProperty(lazyLoadIndex);
    LoadBeanRequest req = new LoadBeanRequest(this, batch, null, batchSize, true, lazyLoadProp, hitCache);
    parent.getEbeanServer().loadBean(req);
  
  }

  /**
   * Check each of the beans in the batch to see if they are in the cache.
   * Get more beans out as necessary to get our desired batch size.
   */
  private List<EntityBeanIntercept> loadBeanCheckBatch(List<EntityBeanIntercept> batch) {
    
    
    List<EntityBeanIntercept> actualLoadBatch = new ArrayList<EntityBeanIntercept>(batchSize);
    List<EntityBeanIntercept> batchToCheck = batch;
    
    int loadedFromCache = 0;
   
    while (true) {
      // check each bean (not already checked) to see if it is in the cache
      for (int i = 0; i < batchToCheck.size(); i++) {
        if (!desc.loadFromCache(batchToCheck.get(i))) {
          actualLoadBatch.add(batchToCheck.get(i));
        } else {
          loadedFromCache++;
          if (logger.isTraceEnabled()) {
            logger.trace( "Loading path:" + fullPath + " - bean loaded from L2 cache(batch)");
          }
        } 
      }

      if (batchToCheck.isEmpty()) {
        // we have exhausted all the beans that need lazy loading
        break;
      }
      int more = batchSize - actualLoadBatch.size();
      if (more <= 0 || loadedFromCache > 500) {
        break;
      }
      // get some more to check as we loaded some from L2 cache
      batchToCheck = weakList.getNextBatch(more);
    }
    return actualLoadBatch;
  }
	
  public void loadSecondaryQuery(OrmQueryRequest<?> parentRequest, int requestedBatchSize, boolean all) {

    synchronized (this) {
      do {
        List<EntityBeanIntercept> batch = weakList.getNextBatch(requestedBatchSize);
        if (batch.size() == 0) {
          // there are no beans to load
          if (logger.isTraceEnabled()) {
            logger.trace("Loading path:" + fullPath + " - no more beans to load");
          }
          return;
        }
        boolean loadCache = false;
        LoadBeanRequest req = new LoadBeanRequest(this, batch, parentRequest.getTransaction(), requestedBatchSize, false, null, loadCache);

        if (logger.isTraceEnabled()) {
          logger.trace("Loading path:" + fullPath + " - secondary query batch load [" + batch.size() + "] beans");
        }

        parent.getEbeanServer().loadBean(req);
        if (!all) {
          break;
        }

      } while (true);
    }
  }
	
}
