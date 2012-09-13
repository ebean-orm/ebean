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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.avaje.ebean.bean.PersistenceContext;
import com.avaje.ebeaninternal.api.Monitor;
import com.avaje.ebeaninternal.server.subclass.SubClassUtil;

/**
 * Default implementation of PersistenceContext.
 * <p>
 * Ensures only one instance of a bean is used according to its type and unique
 * id.
 * </p>
 * <p>
 * PersistenceContext lives on a Transaction and as such is expected to only have
 * a single thread accessing it at a time. This is not expected to be used concurrently.
 * </p>
 * <p>
 * Duplicate beans are ones having the same type and unique id value. These are
 * considered duplicates and replaced by the bean instance that was already
 * loaded into the PersistanceContext.
 * </p>
 */
public final class DefaultPersistenceContext implements PersistenceContext {

    /**
     * Map used hold caches. One cache per bean type.
     */
    private final HashMap<String,ClassContext> typeCache = new HashMap<String,ClassContext>();

    private final Monitor monitor = new Monitor();
    
    /**
     * Create a new PersistanceContext.
     */
    public DefaultPersistenceContext() {
    }

    /**
     * Set an object into the PersistanceContext.
     */
    public void put(Object id, Object bean) {
    	synchronized (monitor) {
    		getClassContext(bean.getClass()).put(id, bean);
    	}
    }
    
    public Object putIfAbsent(Object id, Object bean){
    	synchronized (monitor) {
    		return getClassContext(bean.getClass()).putIfAbsent(id, bean);
    	}
    }

    

    /**
     * Return an object given its type and unique id.
     */
    public Object get(Class<?> beanType, Object id) {
    	synchronized (monitor) {
    		return getClassContext(beanType).get(id);
    	}
    }

    /**
     * Return the number of beans of the given type in the persistence context.
     */
    public int size(Class<?> beanType) {
        synchronized (monitor) {
            ClassContext classMap = typeCache.get(beanType.getName());
            return classMap == null ? 0 : classMap.size();
        }
    }
    
    /**
     * Clear the PersistenceContext.
     */
    public void clear() {
    	synchronized (monitor) {
    		typeCache.clear();
    	}
    }

    public void clear(Class<?> beanType) {
    	synchronized (monitor) {
	    	ClassContext classMap = typeCache.get(beanType.getName());
	        if (classMap != null) {
	        	classMap.clear();	
	        }
    	}
    }

    public void clear(Class<?> beanType, Object id) {
    	synchronized (monitor) {
	    	ClassContext classMap = typeCache.get(beanType.getName());
	        if (classMap != null && id != null) {
	            //id = getUid(beanType, id);
	            classMap.remove(id);
	        }
    	}
    }
    
    public String toString() {
        synchronized (monitor) {
            StringBuilder sb = new StringBuilder();
            Iterator<Entry<String, ClassContext>> it = typeCache.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, ClassContext> entry = it.next();
                if (entry.getValue().size() > 0){
                    sb.append(entry.getKey()+":"+entry.getValue().size()+"; ");
                }
            }
            return sb.toString();
        }
    }
   
    private ClassContext getClassContext(Class<?> beanType) {

    	// strip off $$EntityBean.. suffix...
    	String clsName = SubClassUtil.getSuperClassName(beanType.getName());
    	
    	ClassContext classMap = typeCache.get(clsName);
        if (classMap == null) {            
            classMap = new ClassContext();
            typeCache.put(clsName, classMap);
        }
        return classMap;
    }

    private static class ClassContext {
    	
        private final WeakValueMap<Object, Object> map = new WeakValueMap<Object, Object>();
        
    	private Object get(Object id){
    	    return map.get(id);
    	}
    	
        private Object putIfAbsent(Object id, Object bean){
            
            return map.putIfAbsent(id, bean);
        }
        
    	private void put(Object id, Object b){
    	    map.put(id, b);
    	}
    	    	
    	private int size() {
    	    return map.size();
    	}
    	
    	private void clear(){
    		map.clear();
    	}
    	
    	private Object remove(Object id){
    		return map.remove(id);
    	}
    }
    
}
