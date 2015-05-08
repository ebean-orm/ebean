package com.avaje.ebeaninternal.server.deploy;

import java.util.List;

import com.avaje.ebean.event.BeanFinder;

/**
 * Factory for controlling the construction of BeanFinders.
 */
public interface BeanFinderManager {
	
	/**
	 * Return the number of beans with a registered finder.
	 */
	public int getRegisterCount();

    /**
     * Create the appropriate BeanController.
     */
    public int createBeanFinders(List<Class<?>> finderClassList);
    
    /**
     * Return the BeanController for a given entity type.
     */
    public <T> BeanFinder<T> getBeanFinder(Class<T> entityType);
}
