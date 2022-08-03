package io.ebeaninternal.server.deploy;

import io.ebean.annotation.ConstraintMode;
import io.ebean.annotation.DbForeignKey;

public final class PropertyForeignKey {

  private final boolean noIndex;
  private final boolean noConstraint;
  private final boolean forceLeftJoin;
  private final ConstraintMode onDelete;
  private final ConstraintMode onUpdate;

  /**
   * Construct for "No Constraint".
   */
  public PropertyForeignKey() {
    this.noConstraint = true;
    this.noIndex = false;
    this.forceLeftJoin = false;
    this.onDelete = ConstraintMode.RESTRICT;
    this.onUpdate = ConstraintMode.RESTRICT;
  }

  /**
   * Construct for the mapping annotation.
   */
  public PropertyForeignKey(DbForeignKey dbForeignKey) {
    this.noIndex = dbForeignKey.noIndex();
    this.noConstraint = dbForeignKey.noConstraint();
    this.forceLeftJoin = dbForeignKey.forceLeftJoin();
    this.onDelete = dbForeignKey.onDelete();
    this.onUpdate = dbForeignKey.onUpdate();
  }

  public boolean isNoIndex() {
    return noIndex;
  }

  public boolean isNoConstraint() {
    return noConstraint;
  }

  public boolean isForceLeftJoin() {
    return forceLeftJoin;
  }

  public ConstraintMode getOnDelete() {
    return onDelete;
  }

  public ConstraintMode getOnUpdate() {
    return onUpdate;
  }
}
