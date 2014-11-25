package com.avaje.ebeaninternal.server.deploy;

import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.Query;
import com.avaje.ebean.Transaction;
import com.avaje.ebean.bean.BeanCollection;
import com.avaje.ebean.bean.BeanCollectionAdd;
import com.avaje.ebean.bean.BeanCollectionLoader;
import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebeaninternal.server.text.json.WriteJson;

import java.io.IOException;

/**
 * Helper functions for performing tasks on Lists Sets or Maps.
 */
public interface BeanCollectionHelp<T> {

	/**
	 * Set the EbeanServer that owns the configuration.
	 */
	public void setLoader(BeanCollectionLoader loader);
	
	/**
	 * Return the mechanism to add beans to the underlying collection.
	 * <p>
	 * For Map's this needs to take the mapKey.
	 * </p>
	 */
	public BeanCollectionAdd getBeanCollectionAdd(Object bc, String mapKey);

  /**
   * Create an empty collection of the correct type without a parent bean.
   */
  public BeanCollection<T> createEmptyNoParent();

	/**
	 * Create an empty collection of the correct type.
	 */
	public BeanCollection<T> createEmpty(EntityBean bean);

	/**
	 * Add a bean to the List Set or Map.
	 */
	public void add(BeanCollection<?> collection, EntityBean bean);

	/**
	 * Create a lazy loading proxy for a List Set or Map.
	 */
	public BeanCollection<T> createReference(EntityBean parentBean);

	/**
	 * Refresh the List Set or Map.
	 */
	public void refresh(EbeanServer server, Query<?> query, Transaction t, EntityBean parentBean);
	
	/**
	 * Apply the new refreshed BeanCollection to the appropriate property of the parent bean.
	 */
	public void refresh(BeanCollection<?> bc, EntityBean parentBean);

  /**
   * Write the collection out as json.
   */
  public void jsonWrite(WriteJson ctx, String name, Object collection, boolean explicitInclude) throws IOException;

}
