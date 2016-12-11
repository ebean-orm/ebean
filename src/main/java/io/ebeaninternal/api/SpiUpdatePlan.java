package io.ebeaninternal.api;

import io.ebean.bean.EntityBean;
import io.ebeaninternal.server.persist.dml.DmlHandler;
import io.ebeaninternal.server.persist.dmlbind.Bindable;

import java.sql.SQLException;

/**
 * A plan for executing bean updates for a given set of changed properties.
 * <p>
 * This is a cachable plan with the purpose of being being able to skip some
 * phases of the update bean processing.
 * </p>
 * <p>
 * The plans are cached by the BeanDescriptors.
 * </>
 */
public interface SpiUpdatePlan {

  /**
   * Return true if the set clause has no columns.
   * <p>
   * Can occur when the only columns updated have a updatable=false in their
   * deployment.
   * </p>
   */
  boolean isEmptySetClause();

  /**
   * Bind given the request and bean. The bean could be the oldValues bean
   * when binding a update or delete where clause with ALL concurrency mode.
   */
  void bindSet(DmlHandler bind, EntityBean bean) throws SQLException;

  /**
   * Return the time this plan was created.
   */
  long getTimeCreated();

  /**
   * Return the time this plan was last used.
   */
  long getTimeLastUsed();

  /**
   * Return the hash key for this plan.
   */
  String getKey();

  /**
   * Return the concurrency mode for this plan.
   */
  ConcurrencyMode getMode();

  /**
   * Return the update SQL statement.
   */
  String getSql();

  /**
   * Return the set of bindable update properties.
   */
  Bindable getSet();

}
