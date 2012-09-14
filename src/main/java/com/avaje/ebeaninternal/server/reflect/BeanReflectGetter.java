package com.avaje.ebeaninternal.server.reflect;

/**
 * The getter implementation for a given bean property.
 */
public interface BeanReflectGetter {

	/**
	 * Return the value of a given bean property.
	 */
	public Object get(Object bean);

	public Object getIntercept(Object bean);

}
