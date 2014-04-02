package com.avaje.ebeaninternal.server.reflect;

/**
 * Provides getter setter and construction methods for beans.
 * <p>
 * This enables the implementation to use standard reflection or
 * code generation.
 * </p>
 */
public interface BeanReflect {

	/**
	 * Create an EntityBean for this type.
	 */
	public Object createEntityBean();
	
	/**
	 * Return the getter for a given bean property.
	 */
	public BeanReflectGetter getGetter(String name, int position);
	
	/**
	 * Return the setter for a given bean property.
	 */
	public BeanReflectSetter getSetter(String name, int position);
}
