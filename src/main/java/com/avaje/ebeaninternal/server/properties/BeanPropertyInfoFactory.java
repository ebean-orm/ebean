package com.avaje.ebeaninternal.server.properties;


/**
 * Creates BeanReflect object used to provide getter setter and construction
 * for the beans.
 */
public interface BeanPropertyInfoFactory {

	/**
	 * Create the BeanReflect for the given plain bean and its EntityBean equivalent.
	 */
	public BeanPropertyInfo create(Class<?> entityBeanType);
}
