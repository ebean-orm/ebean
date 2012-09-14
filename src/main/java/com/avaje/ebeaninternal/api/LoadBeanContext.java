package com.avaje.ebeaninternal.api;

import com.avaje.ebean.bean.PersistenceContext;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;

/**
 * Controls the loading of ManyToOne and OneToOne relationships.
 *  
 * @author rbygrave
 */
public interface LoadBeanContext extends LoadSecondaryQuery {
	
	/**
	 * Configure the query to load beans for this node/path.
	 */
	public void configureQuery(SpiQuery<?> query, String lazyLoadProperty);
	
	/**
	 * Return the full path of this node from the root object.
	 */
	public String getFullPath();
	
	/**
	 * Return the persistence context used for all queries 
	 * related to this object graph.
	 */
	public PersistenceContext getPersistenceContext();

	/**
	 * Return the BeanDescriptor for beans for this node.
	 */
	public BeanDescriptor<?> getBeanDescriptor();

	/**
	 * Return the batchSize used for lazy loading beans.
	 */
	public int getBatchSize();
	
}
