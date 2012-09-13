package com.avaje.ebeaninternal.server.cache;

import java.util.HashSet;
import java.util.Set;

import com.avaje.ebeaninternal.server.core.PersistRequestBean;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.deploy.BeanProperty;

public class CachedBeanDataUpdate {

    public static CachedBeanData update(BeanDescriptor<?> desc, CachedBeanData data, PersistRequestBean<?> updateRequest){

    	
    	Set<String> loadedProperties = data.getLoadedProperties();
    	Object[] copyOfData = data.copyData();
    	
    	Object updateBean = updateRequest.getBean();
    	Set<String> updatedProperties = updateRequest.getUpdatedProperties();
    	
    	int naturalKeyUpdate = -1;
    	boolean mergeProperties = false;
    	BeanProperty[] props = desc.propertiesNonMany();
    	for (int i = 0; i < props.length; i++) {
	        if (updatedProperties.contains(props[i].getName())){
	        	if (props[i].isNaturalKey()){
	        		naturalKeyUpdate = i;
	        	}
	        	copyOfData[i] = props[i].getCacheDataValue(updateBean);
	        	if (loadedProperties != null && !mergeProperties && !loadedProperties.contains(props[i].getName())){
	        		mergeProperties = true;
	        	}
	        }
        }
    	
    	if (mergeProperties){
    		HashSet<String> mergeProps = new HashSet<String>();
    		mergeProps.addAll(loadedProperties);
    		mergeProps.addAll(updatedProperties);
    		loadedProperties = mergeProps;
    	}
    	
    	return new CachedBeanData(null, loadedProperties, copyOfData, naturalKeyUpdate);
        
    }
    
    

}