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
package com.avaje.ebeaninternal.server.persist;

import java.util.ArrayList;
import java.util.List;

import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.ebeaninternal.api.SpiQuery;
import com.avaje.ebeaninternal.api.SpiTransaction;
import com.avaje.ebeaninternal.server.core.PersistRequestBean;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.deploy.BeanPropertyAssocOne;
import com.avaje.ebeaninternal.server.transaction.DefaultPersistenceContext;

/**
 * Used for deletion of a partially populated bean where some cascade delete
 * properties where not loaded.
 * <p>
 * This bean effectively holds the foreign properties that where not loaded, and
 * helps fetch the foreign keys and delete the appropriate rows.
 * </p>
 * 
 * @author rbygrave
 */
public class DeleteUnloadedForeignKeys {

    private final List<BeanPropertyAssocOne<?>> propList = new ArrayList<BeanPropertyAssocOne<?>>(4);
    
    private final SpiEbeanServer server; 
    
    private final PersistRequestBean<?> request;

    private Object beanWithForeignKeys;
    
    public DeleteUnloadedForeignKeys(SpiEbeanServer server, PersistRequestBean<?> request) {
        this.server = server;
        this.request = request;
    }
    
    public boolean isEmpty() {
        return propList.isEmpty();
    }
    
    public void add(BeanPropertyAssocOne<?> prop) {
        propList.add(prop);
    }

    /**
     * Execute a query fetching the missing (unloaded) foreign keys. We need to
     * fetch these key values before the parent bean is deleted.
     */
    public void queryForeignKeys() {

        BeanDescriptor<?> descriptor = request.getBeanDescriptor();
        SpiQuery<?> q = (SpiQuery<?>) server.createQuery(descriptor.getBeanType());

        Object id = request.getBeanId();

        StringBuilder sb = new StringBuilder(30);
        for (int i = 0; i < propList.size(); i++) {
            sb.append(propList.get(i).getName()).append(",");
        }

        // run query in a separate persistence context
        q.setPersistenceContext(new DefaultPersistenceContext());
        q.setAutofetch(false);
        q.select(sb.toString());
        q.where().idEq(id);

        SpiTransaction t = request.getTransaction();
        if (t.isLogSummary()) {
        	t.logInternal("-- Ebean fetching foreign key values for delete of " + descriptor.getName() + " id:" + id);
        }
        beanWithForeignKeys = server.findUnique(q, t);
    }

    /**
     * Delete the rows relating to the foreign keys. These deletions occur after
     * the parent bean has been deleted.
     */
    public void deleteCascade() {

        for (int i = 0; i < propList.size(); i++) {
            BeanPropertyAssocOne<?> prop = propList.get(i);
            Object detailBean = prop.getValue(beanWithForeignKeys);

            // if bean exists with a unique id then delete it
            if (detailBean != null && prop.hasId(detailBean)) {
                server.delete(detailBean, request.getTransaction());
            }
        }
    }
}
