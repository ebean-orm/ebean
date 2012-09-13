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
package com.avaje.ebeaninternal.server.persist.dmlbind;

import java.util.ArrayList;
import java.util.List;

import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.deploy.BeanProperty;
import com.avaje.ebeaninternal.server.deploy.BeanPropertyAssocOne;
import com.avaje.ebeaninternal.server.persist.dml.DmlMode;

/**
 * A factory that builds Bindable for embedded bean properties.
 */
public class FactoryEmbedded {

	private final FactoryProperty factoryProperty;

	public FactoryEmbedded(boolean bindEncryptDataFirst) {
		factoryProperty = new FactoryProperty(bindEncryptDataFirst);
	}
	
	/**
	 * Add bindable for the embedded properties to the list.
	 */
	public void create(List<Bindable> list, BeanDescriptor<?> desc, DmlMode mode, boolean withLobs) {
		
		BeanPropertyAssocOne<?>[] embedded = desc.propertiesEmbedded();
				
		for (int j = 0; j < embedded.length; j++) {
		
			List<Bindable> bindList = new ArrayList<Bindable>();
			
			BeanProperty[] props = embedded[j].getProperties();
			for (int i = 0; i < props.length; i++) {
				Bindable item = factoryProperty.create(props[i], mode, withLobs);
				if (item != null){
					bindList.add(item);
				}
			}
			
			list.add(new BindableEmbedded(embedded[j], bindList));
		}
	}
	

}
