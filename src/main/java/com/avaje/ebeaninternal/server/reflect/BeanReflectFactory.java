package com.avaje.ebeaninternal.server.reflect;


/**
 * Creates BeanReflect object used to provide getter setter and construction
 * for the beans.
 */
public interface BeanReflectFactory {

	/**
	 * Create the BeanReflect for the given plain bean and its EntityBean equivalent.
	 */
	public BeanReflect create(Class<?> entityBeanType);
}
