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
