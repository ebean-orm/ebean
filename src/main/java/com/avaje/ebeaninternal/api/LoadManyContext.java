package com.avaje.ebeaninternal.api;

import com.avaje.ebean.bean.ObjectGraphNode;
import com.avaje.ebean.bean.PersistenceContext;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.deploy.BeanPropertyAssocMany;

/**
 * Controls the loading of OneToMany and ManyToMany relationships.
 *  
 * @author rbygrave
 */
public interface LoadManyContext extends LoadSecondaryQuery {

	/**
	 * Configure the query to load beans for this node/path.
	 */
	public void configureQuery(SpiQuery<?> query);
	
	/**
	 * Return the full path of this node from the root object.
	 */
	public String getFullPath();
	
	/**
	 * Return the node location for this node/path.
	 */
	public ObjectGraphNode getObjectGraphNode();
	
	
	/**
	 * Return the persistence context used for all queries 
	 * related to this object graph.
	 */
	public PersistenceContext getPersistenceContext();

	/**
	 * Return the batchSize used for lazy loading beans.
	 */
	public int getBatchSize();

	/**
	 * Return the BeanDescriptor for beans for this node.
	 */
	public BeanDescriptor<?> getBeanDescriptor();

	/**
	 * Return the associated Many bean property.
	 */
	public BeanPropertyAssocMany<?> getBeanProperty();


	
}
