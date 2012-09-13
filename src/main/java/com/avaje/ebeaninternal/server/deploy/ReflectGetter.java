/**
 * Copyright (C) 2009 Authors
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
package com.avaje.ebeaninternal.server.deploy;

import java.lang.reflect.Method;

import com.avaje.ebeaninternal.server.deploy.meta.DeployBeanProperty;
import com.avaje.ebeaninternal.server.reflect.BeanReflectGetter;

/**
 * For abstract classes that hold the id property we need to
 * use reflection to get the id values some times.
 * <p>
 * This provides the BeanReflectGetter objects to do that.
 * </p>
 * @author rbygrave
 */
public class ReflectGetter {

	/**
	 * Create a reflection based BeanReflectGetter for getting the
	 * id from abstract inheritance hierarchy object.
	 */
	public static BeanReflectGetter create(DeployBeanProperty prop) {
			
		if (!prop.isId()){
			// not expecting this to ever be used/called
			return new NonIdGetter(prop.getFullBeanName());
			
		} else {
			String property = prop.getFullBeanName();
			Method readMethod = prop.getReadMethod();
			if (readMethod == null){
				String m = "Abstract class with no readMethod for "+property;
				throw new RuntimeException(m);
			}
			return new IdGetter(property, readMethod);
		} 
	}
	
	public static class IdGetter implements BeanReflectGetter {

		public static final Object[] NO_ARGS = new Object[0];
		
		private final Method readMethod;
		private final String property;
		
		public IdGetter(String property, Method readMethod) {
			this.property = property;
			this.readMethod = readMethod;
		}
		
		public Object get(Object bean) {
			try {
				return readMethod.invoke(bean, NO_ARGS);
			} catch (Exception e) {
				String m = "Error on ["+property+"] using readMethod "+readMethod;
				throw new RuntimeException(m, e);
			} 
		}
	
		public Object getIntercept(Object bean) {
			return get(bean);
		}
	}

	public static class NonIdGetter implements BeanReflectGetter {
		
		private final String property;
		
		public NonIdGetter(String property) {
			this.property = property;
		}
		
		public Object get(Object bean) {
			
			String m = "Not expecting this method to be called on ["+property
				+"] as it is a NON ID property on an abstract class";
			throw new RuntimeException(m); 
		}

		public Object getIntercept(Object bean) {
			return get(bean);
		}
	}
	
}
