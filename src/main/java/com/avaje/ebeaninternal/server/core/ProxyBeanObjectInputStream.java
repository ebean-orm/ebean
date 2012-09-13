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
package com.avaje.ebeaninternal.server.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;

import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.bean.SerializeControl;
import com.avaje.ebeaninternal.api.ClassUtil;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.subclass.SubClassUtil;

/**
 * Read an ObjectInputStream potentially containing "proxy" / "subclassed"
 * entity objects.
 * <p>
 * This does not need to be used for "Enhanced" beans... but if you want to
 * deserialise "proxy" / "subclassed" beans you need to use this
 * ProxyBeanObjectInputStream. The reason is because it is required to resolve
 * the class (The class with the $$EntityBean suffix). As this class is in
 * another class loader typically as plain ObjectInputStream is unable to resolve
 * the class - and hence we need to use this ProxyBeanObjectInputStream.
 * </p>
 */
public class ProxyBeanObjectInputStream extends ObjectInputStream {

	private final SpiEbeanServer ebeanServer;

	/**
	 * Create with a given InputStream and EbeanServer.
	 * <p>
	 * The EbeanServer should be the one that created the 'proxy' classes that
	 * were serialised.
	 * </p>
	 */
	public ProxyBeanObjectInputStream(InputStream in, EbeanServer ebeanServer)
			throws IOException {
		
		super(in);
		this.ebeanServer = (SpiEbeanServer) ebeanServer;
		SerializeControl.setVanilla(false);
	}

	/**
	 * close and reset the serialization mode.
	 * <p>
	 * uses SerializeControl.resetToDefault().
	 * </p>
	 */
	public void close() throws IOException {
		super.close();
		SerializeControl.resetToDefault();
	}

	/**
	 * Resolve the generated Class potentially using reading the embedded
	 * MethodInfo.
	 */
	protected Class<?> resolveGenerated(ObjectStreamClass desc)
			throws IOException, ClassNotFoundException {

		String className = desc.getName();

		String vanillaClassName = SubClassUtil.getSuperClassName(className);
		Class<?> vanillaClass = ClassUtil.forName(vanillaClassName, this.getClass());

		BeanDescriptor<?> d = ebeanServer.getBeanDescriptor(vanillaClass);
		if (d == null) {
			String msg = "Could not find BeanDescriptor for "+ vanillaClassName;
			throw new IOException(msg);
		} else {
			return d.getFactoryType();
		}
	}

	/**
	 * checks for generated subclasses and handles them appropriately.
	 */
	protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException,
			ClassNotFoundException {

		String className = desc.getName();
		if (SubClassUtil.isSubClass(className)) {
			return resolveGenerated(desc);
		}

		return super.resolveClass(desc);
	}

}
