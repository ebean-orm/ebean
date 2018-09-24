package io.ebeaninternal.server.core;

import io.ebean.CallableSql;
import io.ebean.MergeOptions;
import io.ebean.Query;
import io.ebean.SqlUpdate;
import io.ebean.Transaction;
import io.ebean.Update;
import io.ebean.bean.EntityBean;
import io.ebean.meta.MetricVisitor;
import io.ebeaninternal.api.SpiSqlUpdate;
import io.ebeaninternal.api.SpiTransaction;
import io.ebeaninternal.server.deploy.BeanDescriptor;

import java.util.Collection;
import java.util.List;

/**
 * API for persisting a bean.
 */
public interface Persister {

  /**
   * Merge the bean.
   */
  int merge(BeanDescriptor<?> desc, EntityBean entityBean, MergeOptions options, SpiTransaction transaction);

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
  int delete(EntityBean entityBean, Transaction t, boolean permanent);

  /**
   * Delete multiple beans given a collection of Id values.
   */
  int deleteMany(Class<?> beanType, Collection<?> ids, Transaction transaction, boolean permanent);

  /**
   * Delete multiple beans when escalated from a delete query.
   */
  int deleteByIds(BeanDescriptor<?> descriptor, List<Object> idList, Transaction transaction, boolean permanent);

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

  /**
   * Visit the metrics.
   */
  void visitMetrics(MetricVisitor visitor);

  /**
   * Add the statement to JDBC batch for later execution via executeBatch.
   */
  void addBatch(SpiSqlUpdate sqlUpdate, SpiTransaction transaction);

  /**
   * Execute the associated batched statement.
   */
  int[] executeBatch(SpiSqlUpdate sqlUpdate, SpiTransaction transaction);
}
