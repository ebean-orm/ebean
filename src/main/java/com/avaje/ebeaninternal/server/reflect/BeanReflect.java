/**
 * Copyright (C) 2006  Robin Bygrave
 * 
 * This file is part of Ebean.
 * 
 * Ebean is free software; you can redistribute it and/or modify it 
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *  
 * Ebean is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Ebean; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA  
 */
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
