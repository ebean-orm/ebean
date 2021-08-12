package io.ebeaninternal.server.persist.dml;

import io.ebean.bean.EntityBean;
import io.ebeaninternal.api.ConcurrencyMode;
import io.ebeaninternal.api.SpiUpdatePlan;
import io.ebeaninternal.server.persist.dmlbind.Bindable;

import java.sql.SQLException;

/**
 * Plan for executing bean updates for a given set of changed properties.
 */
class UpdatePlan implements SpiUpdatePlan {

  private final String key;
  private final ConcurrencyMode mode;
  private final String sql;
  private final Bindable set;
  private final long timeCreated;
  private final boolean emptySetClause;
  private long timeLastUsed;

  /**
   * Create a UpdatePlan with a given key.
   */
  UpdatePlan(String key, ConcurrencyMode mode, String sql, Bindable set) {
    this.emptySetClause = (sql == null);
    this.key = key;
    this.mode = mode;
    this.sql = sql;
    this.set = set;
    this.timeCreated = System.currentTimeMillis();
  }

  @Override
  public boolean isEmptySetClause() {
    return emptySetClause;
  }

  @Override
  public void bindSet(DmlHandler bind, EntityBean bean) throws SQLException {
    set.dmlBind(bind, bean);
    // atomic on 64 bit jvm
    this.timeLastUsed = System.currentTimeMillis();
  }

  @Override
  public long getTimeCreated() {
    return timeCreated;
  }

  @Override
  public long getTimeLastUsed() {
    return timeLastUsed;
  }

  @Override
  public String getKey() {
    return key;
  }

  @Override
  public ConcurrencyMode getMode() {
    return mode;
  }

  @Override
  public String getSql() {
    return sql;
  }

  @Override
  public Bindable getSet() {
    return set;
  }

}
