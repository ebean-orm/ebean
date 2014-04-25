package com.avaje.ebeaninternal.server.deploy;

import java.util.Map;

import com.avaje.ebean.bean.BeanCollection;
import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebean.bean.EntityBeanIntercept;
import com.avaje.ebean.bean.PersistenceContext;
import com.avaje.ebeaninternal.api.SpiQuery;
import com.avaje.ebeaninternal.server.type.DataReader;

/**
 * Context provided when a BeanProperty reads from a ResultSet.
 */
public interface DbReadContext {
	
  /**
   * Return the state of the object graph.
   */
  public Boolean isReadOnly();
  
  /**
   * Propagate the state to the bean.
   */
  public void propagateState(Object e);
    
  /**
   * Return the DataReader.
   */
  public DataReader getDataReader();
 	
	/**
	 * Return true if the query is using supplied SQL rather than generated SQL.
	 */
	public boolean isRawSql();
	
	/**
	 * Set the JoinNode - used by proxy/reference beans for profiling.
	 */
	public void setCurrentPrefix(String currentPrefix, Map<String,String> pathMap);

	/**
	 * Return true if we are profiling this query.
	 */
	public boolean isAutoFetchProfiling();
	
	/**
	 * Add autoFetch profiling for a loaded entity bean.
	 */
	public void profileBean(EntityBeanIntercept ebi, String prefix);
	
	/**
	 * Return the persistence context. 
	 */
	public PersistenceContext getPersistenceContext();

	/**
	 * Register a reference for lazy loading.
	 */
	public void register(String path, EntityBeanIntercept ebi);

	/**
	 * Register a collection for lazy loading.
	 */
	public void register(String path, BeanCollection<?> bc);

	/**
	 * Return the property that is associated with the many. There can only be
	 * one. This can be null.
	 */
	public BeanPropertyAssocMany<?> getManyProperty();

	/**
	 * Set back the bean that has just been loaded with its id.
	 */
	public void setLoadedBean(EntityBean loadedBean, Object id, Object lazyLoadParentId);

	/**
	 * Set back the 'detail' bean that has just been loaded.
	 */
	public void setLoadedManyBean(EntityBean loadedBean);
	
	/**
	 * Return the query mode.
	 */
	public SpiQuery.Mode getQueryMode();
}
