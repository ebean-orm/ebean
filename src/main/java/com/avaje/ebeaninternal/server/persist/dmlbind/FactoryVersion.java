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

/**
 * Creates a Bindable to support version concurrency where clauses.
 */
public class FactoryVersion {

	
	public FactoryVersion() {
	}
	
	/**
	 * Create a Bindable for the version property(s) for a bean type.
	 */
	public Bindable create(BeanDescriptor<?> desc) {

		List<Bindable> verList = new ArrayList<Bindable>();
		
		BeanProperty[] vers = desc.propertiesVersion();
		for (int i = 0; i < vers.length; i++) {
			verList.add(new BindableProperty(vers[i]));
		}

		// version columns on embedded beans?
		BeanPropertyAssocOne<?>[] embedded = desc.propertiesEmbedded();
		for (int j = 0; j < embedded.length; j++) {

			if (embedded[j].isEmbeddedVersion()) {
				
				List<Bindable> bindList = new ArrayList<Bindable>();
				
				BeanProperty[] embProps = embedded[j].getProperties();
				
				for (int i = 0; i < embProps.length; i++) {
					if (embProps[i].isVersion()){
						bindList.add(new BindableProperty(embProps[i]));						
					}
				}
				
				verList.add(new BindableEmbedded(embedded[j], bindList));
			}
		}
		
		if (verList.size() == 0){
			return null;
		}
		if (verList.size() == 1){
			return verList.get(0);
		}
		
		return new BindableList(verList);
	}
}
