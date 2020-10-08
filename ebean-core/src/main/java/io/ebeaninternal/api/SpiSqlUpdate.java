package io.ebeaninternal.api;

import io.ebean.SqlUpdate;

public interface SpiSqlUpdate extends SqlUpdate {

  /**
   * Return the sql taking into account bind parameter expansion.
   */
  String getBaseSql();

  /**
   * Return the Bind parameters.
   */
  BindParams getBindParams();

  /**
   * Set the final sql being executed with named parameters replaced etc.
   */
  void setGeneratedSql(String sql);

  /**
   * Return true if we are using getGeneratedKeys.
   */
  boolean isGetGeneratedKeys();

  /**
   * Set the generated key value.
   */
  void setGeneratedKey(Object idValue);

  /**
   * Reset bind position to be ready for another bind execute.
   */
  void reset();

  /**
   * Return a copy of the SqlUpdate with empty bind parameters.
   */
  SpiSqlUpdate copy();
}
