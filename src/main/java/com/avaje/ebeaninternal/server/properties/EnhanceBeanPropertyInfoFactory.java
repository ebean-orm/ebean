package com.avaje.ebeaninternal.server.properties;

/**
 * Creates a BeanReflectFactory based on the enhancement that 
 * creates EntityBean implementations.
 */
public final class EnhanceBeanPropertyInfoFactory implements BeanPropertyInfoFactory {

	public BeanPropertyInfo create(Class<?> entityBeanType) {
		return new EnhanceBeanPropertyInfo(entityBeanType);
	}

}
