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
package com.avaje.ebeaninternal.server.type;

import com.avaje.ebean.config.CompoundTypeProperty;

/**
 * Wraps a CompoundTypeProperty with it's type and parent for nested compound
 * types.
 * 
 * @author rbygrave
 */
public class CtCompoundProperty {

    private final String relativeName;

    private final CtCompoundProperty parent;

    private final CtCompoundType<?> compoundType;

    @SuppressWarnings({ "rawtypes" })
    private final CompoundTypeProperty property;

    public CtCompoundProperty(String relativeName, CtCompoundProperty parent, CtCompoundType<?> ctType,
            CompoundTypeProperty<?, ?> property) {

        this.relativeName = relativeName;
        this.parent = parent;
        this.compoundType = ctType;
        this.property = property;
    }

    /**
     * The property name relative to the root of the compound type.
     */
    public String getRelativeName() {
        return relativeName;
    }

    /**
     * The property name local to its type.
     */
    public String getPropertyName() {
        return property.getName();
    }

    public String toString() {
        return relativeName;
    }

    @SuppressWarnings("unchecked")
    public Object getValue(Object valueObject) {
        if (valueObject == null) {
            return null;
        }
        if (parent != null) {
            valueObject = parent.getValue(valueObject);
        }
        return property.getValue(valueObject);
    }

    /**
     * Set a scalar value that is used to build the immutable compound value
     * object.
     * <p>
     * When all the scalar values have been collected then the compound value
     * object is built and this can be recursive for nested compound types.
     * </p>
     */
    public Object setValue(Object bean, Object value) {

        // compoundType and propertyName should be correct depth
        Object compoundValue = ImmutableCompoundTypeBuilder.set(compoundType, property.getName(), value);

        if (compoundValue != null && parent != null) {
            // Continue up the tree
            return parent.setValue(bean, compoundValue);

        } else {
            return compoundValue;
        }
    }

}
