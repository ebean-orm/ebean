package com.avaje.ebeaninternal.api;

import java.sql.SQLException;

import com.avaje.ebean.annotation.ConcurrencyMode;
import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebeaninternal.server.persist.dml.DmlHandler;
import com.avaje.ebeaninternal.server.persist.dmlbind.Bindable;

/**
 * A plan for executing bean updates for a given set of changed properties.
 * <p>
 * This is a cachable plan with the purpose of being being able to skip some
 * phases of the update bean processing.
 * </p>
 * <p>
 * The plans are cached by the BeanDescriptors.
 * </>
 * 
 * @author rbygrave
 */
public interface SpiUpdatePlan {

  /**
   * Return true if the set clause has no columns.
   * <p>
   * Can occur when the only columns updated have a updatable=false in their
   * deployment.
   * </p>
   */
  public boolean isEmptySetClause();
    
	/**
	 * Bind given the request and bean. The bean could be the oldValues bean
	 * when binding a update or delete where clause with ALL concurrency mode.
	 */
	public void bindSet(DmlHandler bind, EntityBean bean) throws SQLException;

	/**
	 * Return the time this plan was created.
	 */
	public long getTimeCreated();

	/**
	 * Return the time this plan was last used.
	 */
	public Long getTimeLastUsed();

	/**
	 * Return the hash key for this plan.
	 */
	public Integer getKey();

	/**
	 * Return the concurrency mode for this plan.
	 */
	public ConcurrencyMode getMode();

	/**
	 * Return the update SQL statement.
	 */
	public String getSql();

	/**
	 * Return the set of bindable update properties.
	 */
	public Bindable getSet();

//	/**
//	 * Return the properties that where changed and should be included in the
//	 * update statement.
//	 */
//	public Set<String> getProperties();

}