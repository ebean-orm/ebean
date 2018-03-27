package io.ebeaninternal.api;

import io.ebean.SqlUpdate;

public interface SpiSqlUpdate extends SqlUpdate {

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
}
