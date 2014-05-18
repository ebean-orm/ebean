package com.avaje.ebeaninternal.api;

import com.avaje.ebeaninternal.server.query.SqlJoinType;

/**
 * Represents a join required for a given property and whether than needs to be an outer join.
 */
public class PropertyJoin {

  /**
   * The property name.
   */
  private final String property;
  
  /**
   * Set to true if the property needs to be an outer join.
   */
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
