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
package com.avaje.ebeaninternal.server.deploy.meta;

import com.avaje.ebean.bean.BeanCollection.ModifyListenMode;
import com.avaje.ebeaninternal.server.deploy.ManyType;
import com.avaje.ebeaninternal.server.type.ScalarType;

public class DeployBeanPropertySimpleCollection<T> extends DeployBeanPropertyAssocMany<T> {

    private final ScalarType<T> collectionScalarType;
    
    public DeployBeanPropertySimpleCollection(DeployBeanDescriptor<?> desc, Class<T> targetType, ScalarType<T> scalarType, ManyType manyType) {
        super(desc, targetType, manyType);
        this.collectionScalarType = scalarType;
        this.modifyListenMode = ModifyListenMode.ALL;
    }

    /**
     * Return the scalarType of the collection elements.
     */
    public ScalarType<T> getCollectionScalarType() {
        return collectionScalarType;
    }

    /**
     * Returns false as never a ManyToMany.
     */
    @Override
    public boolean isManyToMany() {
        return false;
    }

    /**
     * Returns true as always Unidirectional.
     */
    @Override
    public boolean isUnidirectional() {
        return true;
    }

    
}
