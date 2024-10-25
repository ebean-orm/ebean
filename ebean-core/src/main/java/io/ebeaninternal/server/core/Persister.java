package io.ebeaninternal.server.core;

import org.jspecify.annotations.Nullable;
import io.ebean.*;
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
   * Perform an Insert using the given bean.
   */
  void insert(EntityBean entityBean, @Nullable InsertOptions insertOptions, @Nullable Transaction t);

  /**
   * Insert or update the bean depending on its state.
   */
  void save(EntityBean entityBean, Transaction t);

  /**
   * Delete a bean given it's type and id value.
   * <p>
   * This will also cascade delete one level of children.
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
   * Execute the SqlUpdate (taking into account transaction batch mode).
   */
  int executeSqlUpdate(SqlUpdate update, Transaction t);

  /**
   * Execute the SqlUpdate now regardless of transaction batch mode.
   */
  int executeSqlUpdateNow(SpiSqlUpdate update, Transaction t);

  /**
   * Execute the CallableSql.
   */
  int executeCallable(CallableSql callable, Transaction t);

  /**
   * Visit the metrics.
   */
  void visitMetrics(MetricVisitor visitor);

  /**
   * Execute or queue the update.
   */
  void executeOrQueue(SpiSqlUpdate update, SpiTransaction t, boolean queue, int queuePosition);

  /**
   * Queue the SqlUpdate for execution with position 0, 1 or 2 defining
   * when it executes relative to the flush of beans .
   */
  void addToFlushQueue(SpiSqlUpdate update, SpiTransaction t, int pos);

  /**
   * Add the statement to JDBC batch for later execution via executeBatch.
   */
  void addBatch(SpiSqlUpdate sqlUpdate, SpiTransaction transaction);

  /**
   * Execute the associated batched statement.
   */
  int[] executeBatch(SpiSqlUpdate sqlUpdate, SpiTransaction transaction);
}
