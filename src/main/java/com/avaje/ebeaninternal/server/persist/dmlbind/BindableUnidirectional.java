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

import javax.persistence.PersistenceException;

import com.avaje.ebeaninternal.server.core.PersistRequestBean;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.deploy.BeanPropertyAssocOne;
import com.avaje.ebeaninternal.server.deploy.id.ImportedId;
import com.avaje.ebeaninternal.server.persist.dml.GenerateDmlRequest;

/**
 * Bindable for a unidirectional relationship.
 * <p>
 * This inserts the foreign key value that is retrieved from the id of the
 * parentBean.
 * </p>
 */
public class BindableUnidirectional implements Bindable {

    private final BeanPropertyAssocOne<?> unidirectional;

    private final ImportedId importedId;

    private final BeanDescriptor<?> desc;

    public BindableUnidirectional(BeanDescriptor<?> desc, BeanPropertyAssocOne<?> unidirectional) {
        this.desc = desc;
        this.unidirectional = unidirectional;
        this.importedId = unidirectional.getImportedId();

    }

    public String toString() {
        return "BindableShadowFKey " + unidirectional;
    }

    public void addChanged(PersistRequestBean<?> request, List<Bindable> list) {
        throw new PersistenceException("Never called (for insert only)");
    }

    public void dmlInsert(GenerateDmlRequest request, boolean checkIncludes) {
        dmlAppend(request, checkIncludes);
    }

    public void dmlAppend(GenerateDmlRequest request, boolean checkIncludes) {
        // always included (in insert)
        importedId.dmlAppend(request);
    }

    public void dmlWhere(GenerateDmlRequest request, boolean checkIncludes, Object bean) {
        throw new RuntimeException("Never called");
    }

    public void dmlBind(BindableRequest request, boolean checkIncludes, Object bean) throws SQLException {
        dmlBind(request, checkIncludes, bean, true);
    }
    
    public void dmlBindWhere(BindableRequest request, boolean checkIncludes, Object bean) throws SQLException {
        dmlBind(request, checkIncludes, bean, false);
    }
    
    private void dmlBind(BindableRequest request, boolean checkIncludes, Object bean, boolean bindNull)
            throws SQLException {

        PersistRequestBean<?> persistRequest = request.getPersistRequest();
        Object parentBean = persistRequest.getParentBean();

        if (parentBean == null) {
            Class<?> localType = desc.getBeanType();
            Class<?> targetType = unidirectional.getTargetType();
            ;
            String msg = "Error inserting bean [" + localType + "] with unidirectional relationship. ";
            msg += "For inserts you must use cascade save on the master bean [" + targetType + "].";
            throw new PersistenceException(msg);
        }

        importedId.bind(request, parentBean, bindNull);
    }

}
