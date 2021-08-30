package io.ebeaninternal.api;

import io.ebeaninternal.server.query.SqlJoinType;

/**
 * Represents a join required for a given property and whether than needs to be an outer join.
 */
public final class PropertyJoin {

  private final String property;
  private final SqlJoinType joinType;

  public PropertyJoin(String property, SqlJoinType joinType) {
    this.property = property;
    this.joinType = joinType;
  }

  /**
   * Return the property that should be joined.
   */
  public String getProperty() {
    return property;
  }

  /**
   * Return true if this join is required to be an outer join.
   */
  public SqlJoinType getSqlJoinType() {
    return joinType;
  }

}
