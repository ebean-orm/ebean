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
package com.avaje.ebeaninternal.server.el;

import com.avaje.ebean.text.StringFormatter;
import com.avaje.ebean.text.StringParser;

/**
 * The expression language object that can get values.
 * <p>
 * This can be used for local sorting and filtering.
 * </p>
 */
public interface ElPropertyValue extends ElPropertyDeploy {

    /**
     * Return the Id values for the given bean value.
     */
    public Object[] getAssocOneIdValues(Object bean);

    /**
     * Return the Id expression string.
     * <p>
     * Typically used to produce id = ? expression strings.
     * </p>
     */
    public String getAssocOneIdExpr(String prefix, String operator);

    /**
     * Return the logical id value expression taking into account embedded id's.
     */
    public String getAssocIdInValueExpr(int size);
        
    /**
     * Return the logical id in expression taking into account embedded id's.
     */
    public String getAssocIdInExpr(String prefix);
    
    /**
     * Return true if this is an ManyToOne or OneToOne associated bean property.
     */
    public boolean isAssocId();

    /**
     * Return true if any path of this path contains a Associated One or Many.
     */
    public boolean isAssocProperty();

    /**
     * Return true if the property is encrypted via Java.
     */
    public boolean isLocalEncrypted();
    
    /**
     * Return true if the property is encrypted in the DB.
     */
    public boolean isDbEncrypted();

    /**
     * Return the deploy order for the property.
     */
    public int getDeployOrder();
    
    /**
     * Return the default StringParser for the scalar property.
     */
    public StringParser getStringParser();

    /**
     * Return the default StringFormatter for the scalar property.
     */
    public StringFormatter getStringFormatter();

    /**
     * Return true if the last type is "DateTime capable" - can support
     * {@link #parseDateTime(long)}.
     */
    public boolean isDateTimeCapable();

    /**
     * Return the underlying JDBC type or 0 if this is not a scalar type.
     */
    public int getJdbcType();
    
    /**
     * For DateTime capable scalar types convert the long systemTimeMillis into
     * an appropriate java time (Date,Timestamp,Time,Calendar, JODA type etc).
     */
    public Object parseDateTime(long systemTimeMillis);

    /**
     * Return the value from a given entity bean.
     */
    public Object elGetValue(Object bean);

    /**
     * Return the value ensuring objects prior to the top scalar property are
     * automatically populated.
     */
    public Object elGetReference(Object bean);

    /**
     * Set a value given a root level bean.
     * <p>
     * If populate then
     * </p>
     */
    public void elSetValue(Object bean, Object value, boolean populate, boolean reference);

    /**
     * Make the owning bean of this property a reference (as in not new/dirty).
     */
    public void elSetReference(Object bean);

    /**
     * Convert the value to the expected type.
     * <p>
     * Typically useful for converting strings to the appropriate number type
     * etc.
     * </p>
     */
    public Object elConvertType(Object value);
}
