package com.avaje.ebeaninternal.server.transaction;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;

public class BeanPathUpdate {

    private final Map<String,BeanPathUpdateIds> map = new LinkedHashMap<String, BeanPathUpdateIds>();

    public void add(BeanDescriptor<?> desc, String path, Object id) {
        
        String key = desc.getFullName()+":"+path;
        BeanPathUpdateIds pathIds = map.get(key);
        if (pathIds == null){
            pathIds = new BeanPathUpdateIds(desc, path);
            map.put(key, pathIds);
        }
        pathIds.addId((Serializable)id);
        
    }
}
