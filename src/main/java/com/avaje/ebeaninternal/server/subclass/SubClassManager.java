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
package com.avaje.ebeaninternal.server.subclass;

import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import javax.persistence.PersistenceException;

import com.avaje.ebean.config.ServerConfig;
import com.avaje.ebean.enhance.agent.EnhanceConstants;
import com.avaje.ebeaninternal.api.ClassUtil;

/**
 * Creates and caches the dynamically generated subclasses.
 * <p>
 * That is, the 'EntityBean' classes are dynamically generated subclasses of the
 * 'vanilla' classes.
 * </p>
 */
public class SubClassManager implements EnhanceConstants {

    private static final Logger logger = Logger.getLogger(SubClassManager.class.getName());

	private final ConcurrentHashMap<String,Class<?>> clzMap;

	private final SubClassFactory subclassFactory;

	private final String serverName;

	/**
	 * The log level for debugging subclass generation/enhancement.
	 */
	private final int logLevel;
	
	/**
	 * Construct with the ClassLoader used to load Ebean.class.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public SubClassManager(ServerConfig serverConfig) {
		
	    String s = serverConfig.getProperty("subClassManager.preferContextClassloader", "true");
	    final boolean preferContext = "true".equalsIgnoreCase(s);
	    
		this.serverName = serverConfig.getName();
		this.logLevel =  serverConfig.getEnhanceLogLevel();
		this.clzMap = new ConcurrentHashMap<String, Class<?>>();
		
		try {
			subclassFactory = (SubClassFactory) AccessController
					.doPrivileged(new PrivilegedExceptionAction() {
						public Object run() {
						    ClassLoader cl = ClassUtil.getClassLoader(this.getClass(), preferContext);
							logger.info("SubClassFactory parent ClassLoader ["+cl.getClass().getName()+"]");
							return new SubClassFactory(cl, logLevel);
						}
					});
		} catch (PrivilegedActionException e) {
			throw new PersistenceException(e);
		}
	}

	/**
	 * Resolve the Class for the class name.
	 * <p>
	 * The methodInfo is used to determine the method interception on the
	 * generated class.
	 * </p>
	 * <p>
	 * If the class has already been generated then it is returned out of a
	 * cache.
	 * </p>
	 */
	public Class<?> resolve(String name) {

		synchronized (this) {
			String superName = SubClassUtil.getSuperClassName(name);	
			Class<?> clz = clzMap.get(superName);
			if (clz == null) {
				clz = createClass(superName);
				clzMap.put(superName, clz);
			}
			return clz;
		}
	}

	private Class<?> createClass(String name) {

		try {

		    Class<?> superClass = Class.forName(name, true, subclassFactory.getParent());

			return subclassFactory.create(superClass, serverName);

		} catch (Exception ex) {
			String m = "Error creating subclass for [" + name + "]";
			throw new PersistenceException(m, ex);
		}
	}

}
