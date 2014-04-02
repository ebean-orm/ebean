package com.avaje.ebeaninternal.server.core;

import java.util.Collection;
import java.util.Set;

import com.avaje.ebean.CallableSql;
import com.avaje.ebean.SqlUpdate;
import com.avaje.ebean.Transaction;
import com.avaje.ebean.Update;
import com.avaje.ebean.bean.EntityBean;


/**
 * API for persisting a bean.
 */
public interface Persister {

    /**
     * Force an Update using the given bean.
     */
    public void forceUpdate(EntityBean entityBean, Set<String> updateProps, Transaction t, boolean deleteMissingChildren, boolean updateNullProperties);

    /**
     * Force an Insert using the given bean.
     */
    public void forceInsert(EntityBean entityBean, Transaction t);

    /**
     * Insert or update the bean depending on its state.
     */
    public void save(EntityBean entityBean, Transaction t);

    /**
     * Save the associations of a ManyToMany given the owner bean and the
     * propertyName of the ManyToMany collection.
     */
    public void saveManyToManyAssociations(EntityBean ownerBean, String propertyName, Transaction t);

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
    public void saveAssociation(EntityBean parentBean, String propertyName, Transaction t);

    /**
     * Delete the associations of a ManyToMany given the owner bean and the property name of the ManyToMany.
     */
    public int deleteManyToManyAssociations(EntityBean ownerBean, String propertyName, Transaction t);

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
    public void delete(EntityBean entityBean, Transaction t);

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
