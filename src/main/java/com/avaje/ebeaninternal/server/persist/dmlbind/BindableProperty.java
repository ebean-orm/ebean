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
import java.util.List;

import com.avaje.ebeaninternal.server.core.PersistRequestBean;
import com.avaje.ebeaninternal.server.deploy.BeanProperty;
import com.avaje.ebeaninternal.server.persist.dml.GenerateDmlRequest;

/**
 * Bindable for a single BeanProperty.
 */
public class BindableProperty implements Bindable {

    protected final BeanProperty prop;

    public BindableProperty(BeanProperty prop) {
        this.prop = prop;
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
        dmlAppend(request, checkIncludes);
    }

    public void dmlAppend(GenerateDmlRequest request, boolean checkIncludes) {
        if (checkIncludes && !request.isIncluded(prop)) {
            return;
        }
        request.appendColumn(prop.getDbColumn());
    }

    /**
     * Used for dynamic where clause generation.
     */
    public void dmlWhere(GenerateDmlRequest request, boolean checkIncludes, Object bean) {
        if (checkIncludes && !request.isIncludedWhere(prop)) {
            return;
        }

        if (bean == null || request.isDbNull(prop.getValue(bean))) {
            request.appendColumnIsNull(prop.getDbColumn());

        } else {
            request.appendColumn(prop.getDbColumn());
        }
    }

    public void dmlBind(BindableRequest request, boolean checkIncludes, Object bean) throws SQLException {

        if (checkIncludes && !request.isIncluded(prop)) {
            return;
        }
        dmlBind(request, bean, true);
    }
    
    public void dmlBindWhere(BindableRequest request, boolean checkIncludes, Object bean) throws SQLException {
        if (checkIncludes && !request.isIncludedWhere(prop)) {
            return;
        }
        dmlBind(request, bean, false);
    }
    
    private void dmlBind(BindableRequest request, Object bean, boolean bindNull)
            throws SQLException {
        
        Object value = null;
        if (bean != null) {
            value = prop.getValue(bean);
        }
        // value = prop.getDefaultValue();
        request.bind(value, prop, prop.getName(), bindNull);
    }
}
