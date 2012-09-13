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
package com.avaje.ebeaninternal.server.core;

import java.util.Collection;
import java.util.Set;

import com.avaje.ebean.CallableSql;
import com.avaje.ebean.SqlUpdate;
import com.avaje.ebean.Transaction;
import com.avaje.ebean.Update;


/**
 * API for persisting a bean.
 */
public interface Persister {

    /**
     * Force an Update using the given bean.
     */
    public void forceUpdate(Object entityBean, Set<String> updateProps, Transaction t, boolean deleteMissingChildren, boolean updateNullProperties);

    /**
     * Force an Insert using the given bean.
     */
    public void forceInsert(Object entityBean, Transaction t);

    /**
     * Insert or update the bean depending on its state.
     */
    public void save(Object entityBean, Transaction t);

    /**
     * Save the associations of a ManyToMany given the owner bean and the
     * propertyName of the ManyToMany collection.
     */
    public void saveManyToManyAssociations(Object ownerBean, String propertyName, Transaction t);

    /**
     * Save an association (OneToMany, ManyToOne, OneToOne or ManyToMany).
     * 
     * @param parentBean
     *            the bean that owns the association.
     * @param propertyName
     *            the name of the property to save.
     * @param t
     *            the transaction to use.
     */
    public void saveAssociation(Object parentBean, String propertyName, Transaction t);

    /**
     * Delete the associations of a ManyToMany given the owner bean and the property name of the ManyToMany.
     */
    public int deleteManyToManyAssociations(Object ownerBean, String propertyName, Transaction t);

    /**
     * Delete a bean given it's type and id value.
     * <p>
     * This will also cascade delete one level of children.
     * </p>
     */
    public int delete(Class<?> beanType, Object id, Transaction transaction);

    /**
     * Delete the bean.
     */
    public void delete(Object entityBean, Transaction t);

    /**
     * Delete multiple beans given a collection of Id values.
     */
    public void deleteMany(Class<?> beanType, Collection<?> ids, Transaction transaction);

    /**
     * Execute the Update.
     */
    public int executeOrmUpdate(Update<?> update, Transaction t);
    
    /**
     * Execute the UpdateSql.
     */
    public int executeSqlUpdate(SqlUpdate update, Transaction t);
    
    /**
     * Execute the CallableSql.
     */
    public int executeCallable(CallableSql callable, Transaction t);
    
}
