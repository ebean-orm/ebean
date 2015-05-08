package com.avaje.ebeaninternal.server.deploy;

import java.util.HashMap;
import java.util.List;

import javax.persistence.PersistenceException;

import com.avaje.ebean.event.BeanFinder;

/**
 * Default implementation for BeanFinderFactory.
 */
public class DefaultBeanFinderManager implements BeanFinderManager {

	HashMap<Class<?>, BeanFinder<?>> registerFor = new HashMap<Class<?>, BeanFinder<?>>();

	public int createBeanFinders(List<Class<?>> finderClassList) {

		for (Class<?> cls : finderClassList) {
			Class<?> entityType = getEntityClass(cls);
			try {
				BeanFinder<?> beanFinder = (BeanFinder<?>) cls.newInstance();				
				registerFor.put(entityType, beanFinder);
				
			} catch (Exception ex) {
				throw new PersistenceException(ex);
			}
		}
		
		return registerFor.size();
	}

	public int getRegisterCount() {
		return registerFor.size();
	}
	
	/**
	 * Return the BeanFinder for a given entity type.
	 */
	@SuppressWarnings("unchecked")
	public <T> BeanFinder<T> getBeanFinder(Class<T> entityType) {
		return (BeanFinder<T>)registerFor.get(entityType);
	}
	
	/**
	 * Find the entity class given the controller class.
	 * <p>
	 * This uses reflection to find the generics parameter type. 
	 * </p>
	 */	
    private Class<?> getEntityClass(Class<?> controller){
    	
		Class<?> cls = ParamTypeUtil.findParamType(controller, BeanFinder.class);
		
		if (cls == null){
			String msg = "Could not determine the entity class (generics parameter type) from "+controller+" using reflection.";
			throw new PersistenceException(msg);
		}
		return cls;
    }
}
