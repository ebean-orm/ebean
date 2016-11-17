package com.avaje.ebeaninternal.server.core;

import com.avaje.ebean.CallableSql;
import com.avaje.ebean.Query;
import com.avaje.ebean.SqlUpdate;
import com.avaje.ebean.Transaction;
import com.avaje.ebean.Update;
import com.avaje.ebean.bean.EntityBean;

import java.util.Collection;
import java.util.List;

/**
 * API for persisting a bean.
 */
public interface Persister {

  /**
   * Update the bean.
   */
  void update(EntityBean entityBean, Transaction t);

  /**
   * Update the bean specifying deleteMissingChildren.
   */
  void update(EntityBean entityBean, Transaction t, boolean deleteMissingChildren);

  /**
   * Force an Insert using the given bean.
   */
  void insert(EntityBean entityBean, Transaction t);

  /**
   * Insert or update the bean depending on its state.
   */
  void save(EntityBean entityBean, Transaction t);

  /**
   * Delete a bean given it's type and id value.
   * <p>
   * This will also cascade delete one level of children.
   * </p>
   */
  int delete(Class<?> beanType, Object id, Transaction transaction, boolean permanent);

  /**
   * Delete the bean.
   */
  boolean delete(EntityBean entityBean, Transaction t, boolean permanent);

  /**
   * Delete multiple beans given a collection of Id values.
   */
  int deleteMany(Class<?> beanType, Collection<?> ids, Transaction transaction, boolean permanent);

  /**
   * Execute the Update.
   */
  int executeOrmUpdate(Update<?> update, Transaction t);

  /**
   * Execute the UpdateSql.
   */
  int executeSqlUpdate(SqlUpdate update, Transaction t);

  /**
   * Execute the CallableSql.
   */
  int executeCallable(CallableSql callable, Transaction t);

  /**
   * Publish the draft beans matching the given query.
   */
  <T> List<T> publish(Query<T> query, Transaction transaction);

  /**
   * Restore the draft beans back to the matching live beans.
   */
  <T> List<T> draftRestore(Query<T> query, Transaction transaction);

}
