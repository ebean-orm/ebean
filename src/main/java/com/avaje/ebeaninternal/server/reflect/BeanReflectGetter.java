package com.avaje.ebeaninternal.server.reflect;

import com.avaje.ebean.bean.EntityBean;

/**
 * The getter implementation for a given bean property.
 */
public interface BeanReflectGetter {

	/**
	 * Return the value of a given bean property.
	 */
	public Object get(EntityBean bean);

	public Object getIntercept(EntityBean bean);

}
