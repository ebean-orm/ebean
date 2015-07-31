package com.avaje.ebeaninternal.server.persist.dml;

import com.avaje.ebean.annotation.ConcurrencyMode;
import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebeaninternal.api.SpiUpdatePlan;
import com.avaje.ebeaninternal.server.persist.dmlbind.Bindable;

import java.sql.SQLException;

/**
 * Plan for executing bean updates for a given set of changed properties.
 */
public class UpdatePlan implements SpiUpdatePlan {

  private final Integer key;

  private final ConcurrencyMode mode;

  private final String sql;

  private final Bindable set;

  private final long timeCreated;

  private final boolean emptySetClause;

  private Long timeLastUsed;

  /**
   * Create a non cached UpdatePlan.
   */
  public UpdatePlan(ConcurrencyMode mode, String sql, Bindable set) {

    this(null, mode, sql, set);
  }

  /**
   * Create a UpdatePlan with a given key.
   */
  public UpdatePlan(Integer key, ConcurrencyMode mode, String sql, Bindable set) {

    this.emptySetClause = (sql == null);
    this.key = key;
    this.mode = mode;
    this.sql = sql;
    this.set = set;
    this.timeCreated = System.currentTimeMillis();
  }

  public boolean isEmptySetClause() {
    return emptySetClause;
  }

  /**
   * Run the prepared statement binding for the 'update set' properties.
   */
  public void bindSet(DmlHandler bind, EntityBean bean) throws SQLException {

    set.dmlBind(bind, bean);

    // not strictly 'thread safe' but object assignment is atomic
    this.timeLastUsed = System.currentTimeMillis();
  }

  /**
   * Return the time this plan was created.
   */
  public long getTimeCreated() {
    return timeCreated;
  }

  /**
   * Return the time this plan was last used.
   */
  public Long getTimeLastUsed() {

    // not thread safe but atomic
    return timeLastUsed;
  }

  /**
   * Return the hash key.
   */
  public Integer getKey() {
    return key;
  }

  /**
   * Return the concurrency mode for this plan.
   */
  public ConcurrencyMode getMode() {
    return mode;
  }

  /**
   * Return the DML statement.
   */
  public String getSql() {
    return sql;
  }

  /**
   * Return the Bindable properties for the update set.
   */
  public Bindable getSet() {
    return set;
  }

}
