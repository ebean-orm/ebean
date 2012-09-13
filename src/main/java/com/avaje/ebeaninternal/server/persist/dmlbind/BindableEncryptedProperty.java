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
package com.avaje.ebeaninternal.server.persist.dmlbind;

import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

import com.avaje.ebeaninternal.server.core.PersistRequestBean;
import com.avaje.ebeaninternal.server.deploy.BeanProperty;
import com.avaje.ebeaninternal.server.persist.dml.GenerateDmlRequest;

/**
 * Bindable for a DB encrypted BeanProperty.
 */
public class BindableEncryptedProperty implements Bindable {

    private final BeanProperty prop;
    
    private final boolean bindEncryptDataFirst;

    public BindableEncryptedProperty(BeanProperty prop, boolean bindEncryptDataFirst) {
        this.prop = prop;
        this.bindEncryptDataFirst = bindEncryptDataFirst;
    }

    public String toString() {
        return prop.toString();
    }

    public void addChanged(PersistRequestBean<?> request, List<Bindable> list) {
        if (request.hasChanged(prop)) {
            list.add(this);
        }
    }

    public void dmlInsert(GenerateDmlRequest request, boolean checkIncludes) {

        if (checkIncludes && !request.isIncluded(prop)) {
            return;
        }
        // columnName
        // AES_ENCRYPT(?,?)
        request.appendColumn(prop.getDbColumn(), prop.getDbBind());
    }

    public void dmlAppend(GenerateDmlRequest request, boolean checkIncludes) {

        if (checkIncludes && !request.isIncluded(prop)) {
            return;
        }
        // columnName = AES_ENCRYPT(?,?)
        request.appendColumn(prop.getDbColumn(), "=", prop.getDbBind());
    }

    /**
     * Used for dynamic where clause generation.
     */
    public void dmlWhere(GenerateDmlRequest request, boolean checkIncludes, Object bean) {
        // only include encrypted property in where when it is included
        // in the update as well (so not using isIncludedWhere)
        if (checkIncludes && !request.isIncluded(prop)) {
            return;
        }

        if (bean == null || request.isDbNull(prop.getValue(bean))) {
            request.appendColumnIsNull(prop.getDbColumn());

        } else {
            // ? = AES_DECRYPT(columnName,?)
            request.appendColumn("? = ", prop.getDecryptSql());
        }
    }

    /**
     * Bind a value in a Insert SET clause.
     */
    public void dmlBind(BindableRequest request, boolean checkIncludes, Object bean)
            throws SQLException {
        
        if (checkIncludes && !request.isIncluded(prop)) {
            return;
        }
        Object value = null;
        if (bean != null) {
            value = prop.getValue(bean);
        }

        // get Encrypt key
        String encryptKeyValue = prop.getEncryptKey().getStringValue(); 

        if (!bindEncryptDataFirst){
            // H2 encrypt function ... different parameter order
            request.bindNoLog(encryptKeyValue, Types.VARCHAR, prop.getName() + "=****");                        
        }
        request.bindNoLog(value, prop, prop.getName(), true);
        
        if (bindEncryptDataFirst){
            // MySql, Postgres, Oracle
            request.bindNoLog(encryptKeyValue, Types.VARCHAR, prop.getName() + "=****");            
        }

    }
    

    /**
     * Bind a value in a Insert SET clause.
     */
    public void dmlBindWhere(BindableRequest request, boolean checkIncludes, Object bean)
            throws SQLException {
        
        if (checkIncludes && !request.isIncluded(prop)) {
            return;
        }
        Object value = null;
        if (bean != null) {
            value = prop.getValue(bean);
        }

        // get Encrypt key
        String encryptKeyValue = prop.getEncryptKey().getStringValue();

        request.bind(value, prop, prop.getName(), false);
        request.bindNoLog(encryptKeyValue, Types.VARCHAR, prop.getName() + "=****");            

    }
}
