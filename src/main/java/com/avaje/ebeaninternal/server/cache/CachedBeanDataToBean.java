package com.avaje.ebeaninternal.server.cache;

import java.util.HashSet;
import java.util.Set;

import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebean.bean.EntityBeanIntercept;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.deploy.BeanProperty;
import com.avaje.ebeaninternal.server.deploy.BeanPropertyAssocMany;

public class CachedBeanDataToBean {

    private final BeanDescriptor<?> desc;
    private final Object bean;
    private final EntityBeanIntercept ebi;
    private final CachedBeanData cacheBeandata;
    private final Set<String> cacheLoadedProperties;
    private final Set<String> loadedProps;
    
    private final Set<String> excludeProps;
    private final Object oldValuesBean;
    private final boolean readOnly;

    public static void load(BeanDescriptor<?> desc, Object bean, CachedBeanData cacheBeandata) {
    	if (bean instanceof EntityBean){
    	    load(desc, bean, ((EntityBean)bean)._ebean_getIntercept(), cacheBeandata);
    	} else {
    	    load(desc, bean, null, cacheBeandata);
    	}
    }

    public static void load(BeanDescriptor<?> desc, Object bean, EntityBeanIntercept ebi, CachedBeanData cacheBeandata) {
    	new CachedBeanDataToBean(desc, bean, ebi, cacheBeandata).load();
    }
    
    private CachedBeanDataToBean(BeanDescriptor<?> desc, Object bean, EntityBeanIntercept ebi, CachedBeanData cacheBeandata) {
        this.desc = desc;
        this.bean = bean;
        this.ebi = ebi;
        this.cacheBeandata = cacheBeandata;
        this.cacheLoadedProperties = cacheBeandata.getLoadedProperties();
        this.loadedProps = (cacheLoadedProperties == null) ? null : new HashSet<String>();
       
        if (ebi != null){
        	this.excludeProps = ebi.getLoadedProps(); 
        	this.oldValuesBean = ebi.getOldValues();
        	this.readOnly = ebi.isReadOnly();
        } else {
        	this.excludeProps = null;
        	this.oldValuesBean = null;
        	this.readOnly = false;
        }
    }
    
    private boolean load(){
        
        BeanProperty[] propertiesNonTransient = desc.propertiesNonMany();
        for (int i = 0; i < propertiesNonTransient.length; i++) {
            BeanProperty prop = propertiesNonTransient[i];
            if (includeNonManyProperty(prop.getName())){
            	Object data = cacheBeandata.getData(i);
                prop.setCacheDataValue(bean, data, oldValuesBean, readOnly);
            }
        }
        BeanPropertyAssocMany<?>[] manys = desc.propertiesMany();
		for (int i = 0; i < manys.length; i++) {
			BeanPropertyAssocMany<?> prop = manys[i];
			if (includeManyProperty(prop.getName())){
				// set a lazy loading proxy
				prop.createReference(bean);				
			}
		}
        
        if (ebi != null){
        	if (loadedProps == null){
            	ebi.setLoadedProps(null);
        	} else {
            	HashSet<String> mergeProps = new HashSet<String>();
            	if (excludeProps != null) {
            		mergeProps.addAll(excludeProps);
            	}
        		mergeProps.addAll(loadedProps);
            	ebi.setLoadedProps(mergeProps);
        	}   
        	ebi.setLoadedLazy();
        }

        return true;
    }
    
    private boolean includeManyProperty(String name) {
        if (excludeProps != null && excludeProps.contains(name)){
            // ignore this property (partial bean lazy loading)
            return false;
        }
        if (loadedProps != null){
            loadedProps.add(name);
        }
        return true;
    }
    
    private boolean includeNonManyProperty(String name) {
        if (excludeProps != null && excludeProps.contains(name)){
            // ignore this property (partial bean lazy loading)
            return false;
        }
        if (cacheLoadedProperties != null && !cacheLoadedProperties.contains(name)){
            return false;
        }
        if (loadedProps != null){
            loadedProps.add(name);
        }
        return true;
    }
    
}