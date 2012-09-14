package com.avaje.ebeaninternal.server.reflect;

/**
 * The setter for a given bean property.
 */
public interface BeanReflectSetter {

	/**
	 * Set the property value of a bean.
	 */
	public void set(Object bean, Object value);

	/**
	 * Set the property value of a bean with interception checks.
	 * <p>
	 * This could invoke lazy loading and or oldValues creation.
	 * </p>
	 */
	public void setIntercept(Object bean, Object value);

}
