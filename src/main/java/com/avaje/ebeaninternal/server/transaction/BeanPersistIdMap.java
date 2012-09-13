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
package com.avaje.ebeaninternal.server.transaction;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import com.avaje.ebeaninternal.server.core.PersistRequest;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;

/**
 * Organises the individual bean persist requests by type.
 */
public final class BeanPersistIdMap {

	private final Map<String,BeanPersistIds> beanMap = new LinkedHashMap<String, BeanPersistIds>();
	
	public String toString() {
	   return beanMap.toString();
	}

	public boolean isEmpty() {
		return beanMap.isEmpty();
	}

    public Collection<BeanPersistIds> values() {
        return beanMap.values();
    }
    
	/**
	 * Add a Insert Update or Delete payload.
	 */
	public void add(BeanDescriptor<?> desc, PersistRequest.Type type, Object id) {
	    
	    BeanPersistIds r = getPersistIds(desc);
		r.addId(type, (Serializable)id);
	}
	
    private BeanPersistIds getPersistIds(BeanDescriptor<?> desc) {
        String beanType = desc.getFullName();
	    BeanPersistIds r = beanMap.get(beanType);
		if (r == null){
			r = new BeanPersistIds(desc);
			beanMap.put(beanType, r);
		}
        return r;
    }

	
}
