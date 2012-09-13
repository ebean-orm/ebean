package com.avaje.ebeaninternal.server.deploy;

import java.util.ArrayList;
import java.util.Iterator;

import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.InvalidValue;
import com.avaje.ebean.Query;
import com.avaje.ebean.Transaction;
import com.avaje.ebean.bean.BeanCollection;
import com.avaje.ebean.bean.BeanCollectionAdd;
import com.avaje.ebean.bean.BeanCollectionLoader;
import com.avaje.ebeaninternal.server.text.json.WriteJsonContext;

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
	 * Create an empty collection of the correct type.
	 */
	public Object createEmpty(boolean vanilla);

	/**
	 * Create an iterator for reading the entries.
	 */
	public Iterator<?> getIterator(Object collection);

	/**
	 * Add a bean to the List Set or Map.
	 */
	public void add(BeanCollection<?> collection, Object bean);

	/**
	 * Create a lazy loading proxy for a List Set or Map.
	 */
	public BeanCollection<T> createReference(Object parentBean, String propertyName);

	/**
	 * Validate the List Set or Map.
	 */
	public ArrayList<InvalidValue> validate(Object manyValue);

	/**
	 * Refresh the List Set or Map.
	 */
	public void refresh(EbeanServer server, Query<?> query, Transaction t, Object parentBean);
	
	/**
	 * Apply the new refreshed BeanCollection to the appropriate property of the parent bean.
	 */
	public void refresh(BeanCollection<?> bc, Object parentBean);

	/**
	 * Write the collection out as json.
	 */
    public void jsonWrite(WriteJsonContext ctx, String name, Object collection, boolean explicitInclude);

}
