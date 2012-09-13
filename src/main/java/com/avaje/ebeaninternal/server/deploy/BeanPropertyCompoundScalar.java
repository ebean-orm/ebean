/**
 * Copyright (C) 2009 Authors
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
package com.avaje.ebeaninternal.server.deploy;

import com.avaje.ebean.config.ScalarTypeConverter;
import com.avaje.ebeaninternal.server.deploy.meta.DeployBeanProperty;
import com.avaje.ebeaninternal.server.type.CtCompoundProperty;

/**
 * A BeanProperty owned by a Compound value object that maps to 
 * a real scalar type.
 * 
 * @author rbygrave
 */
public class BeanPropertyCompoundScalar extends BeanProperty {

    private final BeanPropertyCompoundRoot rootProperty;
    
    private final CtCompoundProperty ctProperty;
    
    @SuppressWarnings("rawtypes")
    private final ScalarTypeConverter typeConverter;
    
    public BeanPropertyCompoundScalar(BeanPropertyCompoundRoot rootProperty, DeployBeanProperty scalarDeploy,
            CtCompoundProperty ctProperty, ScalarTypeConverter<?, ?> typeConverter) {
        
        super(scalarDeploy);
        this.rootProperty = rootProperty;
        this.ctProperty = ctProperty;
        this.typeConverter = typeConverter;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object getValue(Object valueObject) {
        if (typeConverter != null){
            valueObject = typeConverter.unwrapValue(valueObject);
        }
        return ctProperty.getValue(valueObject);
    }

    @Override
    public void setValue(Object bean, Object value) {
        setValueInCompound(bean, value, false);        
    }
    
    @SuppressWarnings("unchecked")
    public void setValueInCompound(Object bean, Object value, boolean intercept) {
        
        Object compoundValue = ctProperty.setValue(bean, value);
        
        if (compoundValue != null){
            if (typeConverter != null){
                compoundValue = typeConverter.wrapValue(compoundValue);
            }
            // we are at the top level and we have a compound value
            // that we can set using the root property
            if (intercept){
                rootProperty.setRootValueIntercept(bean, compoundValue);
            } else {
                rootProperty.setRootValue(bean, compoundValue);
            }
        }
    }

    /**
     * No interception on embedded scalar values inside a CVO.
     */
    @Override
    public void setValueIntercept(Object bean, Object value) {
        setValueInCompound(bean, value, true);
    }

    /**
     * No interception on embedded scalar values inside a CVO.
     */
    @Override
    public Object getValueIntercept(Object bean) {
        return getValue(bean);
    }

    @Override
    public Object elGetReference(Object bean) {
        return getValue(bean);
    }

    @Override
    public Object elGetValue(Object bean) {
        return getValue(bean);
    }

    @Override
    public void elSetReference(Object bean) {
        super.elSetReference(bean);
    }

    @Override
    public void elSetValue(Object bean, Object value, boolean populate, boolean reference) {
        super.elSetValue(bean, value, populate, reference);
    }

    
}
