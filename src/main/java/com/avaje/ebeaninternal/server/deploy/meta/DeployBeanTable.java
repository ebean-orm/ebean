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
package com.avaje.ebeaninternal.server.deploy.meta;

import java.util.List;

import com.avaje.ebeaninternal.server.deploy.BeanDescriptorMap;
import com.avaje.ebeaninternal.server.deploy.BeanProperty;
import com.avaje.ebeaninternal.server.deploy.BeanPropertyAssocOne;


/**
 * Used for associated beans in place of a BeanDescriptor. This is done to avoid
 * recursion issues due to the potentially bi-directional and circular
 * relationships between beans.
 * <p>
 * It holds the main deployment information and not all the detail that is held
 * in a BeanDescriptor.
 * </p>
 */
public class DeployBeanTable {

    private final Class<?> beanType;

    /**
     * The base table.
     */
    private String baseTable;

    private List<DeployBeanProperty> idProperties;
    
    /**
     * Create the BeanTable.
     */
    public DeployBeanTable(Class<?> beanType) {
        this.beanType = beanType;
    }
    
    /**
     * Return the base table for this BeanTable.
     * This is used to determine the join information
     * for associations.
     */
    public String getBaseTable() {
        return baseTable;
    }

    /**
     * Set the base table for this BeanTable.
     */
    public void setBaseTable(String baseTable) {
        this.baseTable = baseTable;
    }
    
    /**
     * Return the id properties.
     */
    public BeanProperty[] createIdProperties(BeanDescriptorMap owner) {
    	BeanProperty[] props = new BeanProperty[idProperties.size()];
    	for (int i = 0; i < idProperties.size(); i++) {
    		props[i] = createProperty(owner, idProperties.get(i));
		}
		return props;
	}
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
	private BeanProperty createProperty(BeanDescriptorMap owner, DeployBeanProperty prop){
    	
    	if (prop instanceof DeployBeanPropertyAssocOne<?>){
    		return new BeanPropertyAssocOne(owner, (DeployBeanPropertyAssocOne<?>)prop);
    		
    	} else {
    		return new BeanProperty(prop);
    	}
    	
    }

    /**
     * Set the Id properties.
     */
	public void setIdProperties(List<DeployBeanProperty> idProperties) {
		this.idProperties = idProperties;
	}

	/**
     * Return the class for this beanTable.
     */
    public Class<?> getBeanType() {
        return beanType;
    }

}
