package com.avaje.ebeaninternal.server.query;

import java.util.Collection;
import java.util.Map;

import com.avaje.ebean.bean.BeanCollection;
import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebeaninternal.api.SpiQuery;
import com.avaje.ebeaninternal.server.core.OrmQueryRequest;
import com.avaje.ebeaninternal.server.core.RelationalQueryRequest;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.deploy.BeanPropertyAssocMany;
import com.avaje.ebeaninternal.server.util.BeanCollectionFactory;
import com.avaje.ebeaninternal.server.util.BeanCollectionParams;

/**
 * Wraps a BeanCollection with helper methods to add beans.
 * <p>
 * Helps adding the bean to the underlying set list or map.
 * </p>
 */
public final class BeanCollectionWrapper {

	/**
	 * Flag set if this builds a Map rather than a Collection.
	 */
	private final boolean isMap;

	/**
	 * The type.
	 */
	private final SpiQuery.Type queryType;

	/**
	 * A property name used as key for a Map.
	 */
	private final String mapKey;

	/**
	 * The actual BeanCollection.
	 */
	private final BeanCollection<?> beanCollection;

	/**
	 * Collection type of BeanCollection.
	 */
	private final Collection<Object> collection;

	/**
	 * Map type of BeanCollection.
	 */
	private final Map<Object,Object> map;

	/**
	 * The associated BeanDescriptor.
	 */
	private final BeanDescriptor<?> desc;

	/**
	 * The number of rows added.
	 */
	private int rowCount;
	
	public BeanCollectionWrapper(RelationalQueryRequest request) {

		this.desc = null;
		this.queryType = request.getQueryType();
		this.mapKey = request.getQuery().getMapKey();
		this.isMap = SpiQuery.Type.MAP.equals(queryType);
		
		this.beanCollection = createBeanCollection(queryType);
		this.collection = getCollection(isMap);
		this.map = getMap(isMap);
	}
	
	/**
	 * Create based on a Find.
	 */
	public BeanCollectionWrapper(OrmQueryRequest<?> request) {

		this.desc = request.getBeanDescriptor();
		this.queryType = request.getQueryType();
		this.mapKey = request.getQuery().getMapKey();
		this.isMap = SpiQuery.Type.MAP.equals(queryType);
		
		this.beanCollection = createBeanCollection(queryType);
		this.collection = getCollection(isMap);
		this.map = getMap(isMap);
	}

	/**
	 * Create based on a ManyType and mapKey. Note the mapKey is only used if
	 * the manyType is a Map.
	 * <p>
	 * modifyListening is set to true if this is a collection used to hold
	 * ManyToMany associated objects.
	 * </p>
	 */
	public BeanCollectionWrapper(BeanPropertyAssocMany<?> manyProp) {
		
		this.queryType = manyProp.getManyType().getQueryType();
		this.mapKey = manyProp.getMapKey();
		this.desc = manyProp.getTargetDescriptor();
		this.isMap = SpiQuery.Type.MAP.equals(queryType);
		
		this.beanCollection = createBeanCollection(queryType);
		this.collection = getCollection(isMap);
		this.map = getMap(isMap);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Map<Object,Object> getMap(boolean isMap) {
		return isMap ? (Map)beanCollection : null;
	}

	@SuppressWarnings("unchecked")
	private Collection<Object> getCollection(boolean isMap) {
		return isMap ? null : (Collection<Object>)beanCollection ;
	}

	/**
	 * Return the underlying BeanCollection.
	 */
	public BeanCollection<?> getBeanCollection() {
		return beanCollection;
	}

	/**
	 * Create a BeanCollection of the correct type.
	 */
	private BeanCollection<?> createBeanCollection(SpiQuery.Type manyType) {
		BeanCollectionParams p = new BeanCollectionParams(manyType);
		return BeanCollectionFactory.create(p);
	}

	/**
	 * Return true if this wraps a Map rather than a set or list.
	 */
	public boolean isMap() {
		return isMap;
	}

	/**
	 * Return the number of rows added to this wrapper.
	 */
	public int size() {
		return rowCount;
	}

	/**
	 * Add the bean to the collection held in this wrapper.
	 */
	public void add(EntityBean bean) {
		add(bean, beanCollection);
	}

	/**
	 * Add the bean to the collection passed.
	 * 
	 * @param bean
	 *            the bean to add
	 * @param collection
	 *            the collection or map to add the bean to
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void add(EntityBean bean, Object collection) {
		if (bean == null) {
			return;
		}
		rowCount++;
		if (isMap) {
			Object keyValue = null;
			if (mapKey != null) {
				// use the value for the property
				keyValue = desc.getValue(bean, mapKey);
			} else {
				// use the uniqueId for this
				keyValue = desc.getId(bean);
			}

			Map mapColl = (Map) collection;
			mapColl.put(keyValue, bean);
		} else {
			((Collection) collection).add(bean);
		}
	}

	/**
	 * Specifically add to a Collection.
	 */
	public void addToCollection(Object bean) {
		collection.add(bean);
	}

	/**
	 * Specifically add to this as a Map with a known key.
	 */
	public void addToMap(Object bean, Object key) {
		map.put(key, bean);
	}

}
