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
package com.avaje.ebeaninternal.server.deploy;

import java.util.List;
import java.util.logging.Logger;

import javax.persistence.PersistenceException;

import com.avaje.ebean.event.BeanPersistListener;
import com.avaje.ebeaninternal.server.core.BootupClasses;
import com.avaje.ebeaninternal.server.deploy.meta.DeployBeanDescriptor;

/**
 * Manages the assignment/registration of BeanPersistListener with their
 * respective DeployBeanDescriptor's.
 */
public class PersistListenerManager {

	private static final Logger logger = Logger.getLogger(PersistListenerManager.class.getName());

	private final List<BeanPersistListener<?>> list;

	public PersistListenerManager(BootupClasses bootupClasses) {
		list = bootupClasses.getBeanPersistListeners();
	}

	public int getRegisterCount() {
		return list.size();
	}

	/**
	 * Return the BeanPersistController for a given entity type.
	 */
	@SuppressWarnings("unchecked")
	public <T> void addPersistListeners(DeployBeanDescriptor<T> deployDesc) {

		for (int i = 0; i < list.size(); i++) {
			BeanPersistListener<?> c = list.get(i);
			if (isRegisterFor(deployDesc.getBeanType(), c)) {
				logger.fine("BeanPersistListener on[" + deployDesc.getFullName() + "] " + c.getClass().getName());
				deployDesc.addPersistListener((BeanPersistListener<T>) c);
			}
		}
	}

	public static boolean isRegisterFor(Class<?> beanType, BeanPersistListener<?> c) {
		Class<?> listenerEntity = getEntityClass(c.getClass());
		return beanType.equals(listenerEntity);
	}
	
	/**
	 * Find the entity class given the controller class.
	 * <p>
	 * This uses reflection to find the generics parameter type.
	 * </p>
	 */
	private static Class<?> getEntityClass(Class<?> controller) {

		Class<?> cls = ParamTypeUtil.findParamType(controller, BeanPersistListener.class);
		if (cls == null) {
			String msg = "Could not determine the entity class (generics parameter type) from " + controller
					+ " using reflection.";
			throw new PersistenceException(msg);
		}
		return cls;
	}
}
