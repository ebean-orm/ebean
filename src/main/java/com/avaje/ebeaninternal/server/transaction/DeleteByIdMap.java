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
import java.util.List;
import java.util.Map;

import com.avaje.ebeaninternal.server.core.PersistRequest;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;

/**
 * Beans deleted by Id used for updating L2 Cache and Lucene indexes.
 */
public final class DeleteByIdMap {

	private final Map<String,BeanPersistIds> beanMap = new LinkedHashMap<String, BeanPersistIds>();
	
	public String toString() {
	   return beanMap.toString();
	}

	public void notifyCache() {
	    for (BeanPersistIds deleteIds : beanMap.values()) {
	        BeanDescriptor<?> d  = deleteIds.getBeanDescriptor();
	        List<Serializable> idValues = deleteIds.getDeleteIds();
	        if (idValues != null){
	            d.queryCacheClear();
	            for (int i = 0; i < idValues.size(); i++) {
                    d.cacheRemove(idValues.get(i));
                }
	        }
        }
	    
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
	public void add(BeanDescriptor<?> desc, Object id) {
	    
	    BeanPersistIds r = getPersistIds(desc);
		r.addId(PersistRequest.Type.DELETE, (Serializable)id);
	}
	
    /**
     * Add a List of Insert Update or Delete Id's.
     */
    public void addList(BeanDescriptor<?> desc, List<Object> idList) {

        BeanPersistIds r = getPersistIds(desc);
        for (int i = 0; i < idList.size(); i++) {
            r.addId(PersistRequest.Type.DELETE, (Serializable) idList.get(i));            
        }
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
