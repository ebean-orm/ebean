package io.ebeaninternal.api;

import io.avaje.lang.Nullable;
import io.ebean.SqlQuery;
import io.ebean.Transaction;

/**
 * SQL query - Internal extension to SqlQuery.
 */
public interface SpiSqlQuery extends SqlQuery, SpiSqlBinding {

  /**
   * Return the transaction explicitly associated to the query.
   */
  @Nullable
  Transaction transaction();
}
