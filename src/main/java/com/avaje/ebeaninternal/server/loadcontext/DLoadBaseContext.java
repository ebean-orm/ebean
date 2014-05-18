package com.avaje.ebeaninternal.server.loadcontext;

import com.avaje.ebean.FetchConfig;
import com.avaje.ebean.bean.ObjectGraphNode;
import com.avaje.ebean.bean.PersistenceContext;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.querydefn.OrmQueryProperties;

/**
 * Base class for Bean and BeanCollection loading (lazy loading and query join loading).
 */
public abstract class DLoadBaseContext {
	
	protected final DLoadContext parent;

	protected final BeanDescriptor<?> desc;
	
	protected final String path;
	
	protected final String fullPath;

	protected final OrmQueryProperties queryProps;
	
	protected final boolean hitCache;
	
	protected final String serverName;
	
	protected final int firstBatchSize;
	
  protected final int secondaryBatchSize;

  protected final ObjectGraphNode objectGraphNode;
  
  protected final boolean queryFetch;
  
  
  public DLoadBaseContext(DLoadContext parent, BeanDescriptor<?> desc, String path, int defaultBatchSize, OrmQueryProperties queryProps) {
    
    this.parent = parent;
    this.serverName = parent.getEbeanServer().getName();
    this.desc = desc;
    this.queryProps = queryProps;
    this.path = path;
    this.fullPath = parent.getFullPath(path);

    this.hitCache = !parent.isExcludeBeanCache() && desc.isBeanCaching();    
        
    this.objectGraphNode = parent.getObjectGraphNode(path);
    
    this.queryFetch =  queryProps != null && queryProps.isQueryFetch();
    this.firstBatchSize = initFirstBatchSize(defaultBatchSize, queryProps);
    this.secondaryBatchSize = initSecondaryBatchSize(defaultBatchSize, firstBatchSize, queryProps);    
  }
	
  private int initFirstBatchSize(int batchSize, OrmQueryProperties queryProps) {
    if (queryProps == null) {
      return batchSize;
    }
    
    int queryFetchBatch = queryProps.getQueryFetchBatch();
    if (queryFetchBatch > 0) {
      // property join was automatically set to a 'query join'
      return queryFetchBatch;
    }
    
    FetchConfig fetchConfig = queryProps.getFetchConfig();
    if (fetchConfig == null) {
      return batchSize;
    }
    
    int queryBatchSize = fetchConfig.getQueryBatchSize();
    if (queryBatchSize == -1) {
      // not eager query fetch, just lazy loading
      return batchSize;

    } else if (queryBatchSize == 0) {
      // default query fetch batch size is 100
      return 100;
    
    } else {
      return queryBatchSize;
    }
  }
  
  private int initSecondaryBatchSize(int defaultBatchSize, int firstBatchSize, OrmQueryProperties queryProps) {
    if (queryProps == null) {
      return defaultBatchSize;
    }
    FetchConfig fetchConfig = queryProps.getFetchConfig();
    if (fetchConfig == null) {
      return defaultBatchSize;
    }
    if (fetchConfig.isQueryAll()) {
      return firstBatchSize;
    }
    
    int lazyBatchSize = fetchConfig.getLazyBatchSize();
    return (lazyBatchSize > 1) ? lazyBatchSize : defaultBatchSize;
  }
  
  protected PersistenceContext getPersistenceContext() {
    return parent.getPersistenceContext();
  }  

}
