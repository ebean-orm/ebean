package com.avaje.ebeaninternal.server.cache;

import java.util.HashSet;
import java.util.Set;

import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebean.bean.EntityBeanIntercept;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.deploy.BeanProperty;

public class CachedBeanDataFromBean {

    private final BeanDescriptor<?> desc;
    private final Object bean;
    private final EntityBeanIntercept ebi;
    
    private final Set<String> loadedProps;
    private final Set<String> extractProps;

    public static CachedBeanData extract(BeanDescriptor<?> desc, Object bean){
    	if (bean instanceof EntityBean){
        	return new CachedBeanDataFromBean(desc, bean, ((EntityBean)bean)._ebean_getIntercept()).extract();    		
    		
    	} else {
        	return new CachedBeanDataFromBean(desc, bean, null).extract();    		
    	}
    }
    
    public static CachedBeanData extract(BeanDescriptor<?> desc, Object bean, EntityBeanIntercept ebi){
    	return new CachedBeanDataFromBean(desc, bean, ebi).extract();
    }
    
    private CachedBeanDataFromBean(BeanDescriptor<?> desc, Object bean, EntityBeanIntercept ebi) {
        this.desc = desc;
        this.bean = bean;
        this.ebi = ebi;        
        if (ebi != null){
        	this.loadedProps = ebi.getLoadedProps(); 
        	this.extractProps = (loadedProps == null) ? null : new HashSet<String>();
        } else {
        	this.extractProps = new HashSet<String>();
        	this.loadedProps = null;
        }
    }
    
    private CachedBeanData extract(){

        BeanProperty[] props = desc.propertiesNonMany();

    	Object[] data = new Object[props.length];
    	
    	int naturalKeyUpdate = -1;
        for (int i = 0; i < props.length; i++) {
        	BeanProperty prop  = props[i];
            if (includeNonManyProperty(prop.getName())){
            	
            	data[i] = prop.getCacheDataValue(bean);
            	if (prop.isNaturalKey()) {
            		naturalKeyUpdate = i;
            	}
            	if (ebi != null){
            		if (extractProps != null){
            			extractProps.add(prop.getName());
            		}
            	} else if (data[i] != null){
            		if (extractProps != null){
            			extractProps.add(prop.getName());
            		}
            	}
            }
        }
        
        Object sharableBean = null;
        if (desc.isCacheSharableBeans() && ebi != null && loadedProps == null){
        	if (ebi.isReadOnly()){
        		sharableBean = bean;
        	} else {
        		// create a readOnly sharable instance by copying the data
        		sharableBean = desc.createBean(false);
        		BeanProperty[] propertiesId = desc.propertiesId();
        		for (int i = 0; i < propertiesId.length; i++) {
        			Object v = propertiesId[i].getValue(bean);
        			propertiesId[i].setValue(sharableBean, v);
                }
        		BeanProperty[] propertiesNonTransient = desc.propertiesNonTransient();
        		for (int i = 0; i < propertiesNonTransient.length; i++) {
        			Object v = propertiesNonTransient[i].getValue(bean);
        			propertiesNonTransient[i].setValue(sharableBean, v);
                }
        		EntityBeanIntercept ebi = ((EntityBean)sharableBean)._ebean_intercept();
        		ebi.setReadOnly(true);
        		ebi.setLoaded();
        	}
        }
        
        return new CachedBeanData(sharableBean, extractProps, data, naturalKeyUpdate);
    }
    
    private boolean includeNonManyProperty(String name) {
    	return loadedProps == null || loadedProps.contains(name);
    }
    
}