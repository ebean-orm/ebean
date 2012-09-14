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
	 * Create a plain vanilla bean for this type.
	 */
	public Object createVanillaBean();
	
	public boolean isVanillaOnly();
	
	/**
	 * Return the getter for a given bean property.
	 */
	public BeanReflectGetter getGetter(String name);
	
	/**
	 * Return the setter for a given bean property.
	 */
	public BeanReflectSetter getSetter(String name);
}
