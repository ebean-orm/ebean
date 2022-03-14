package io.ebeaninternal.server.query;

import io.ebeaninternal.server.deploy.DbSqlContext;

/**
 * Extra Sql joins conditionally added if required (after children are joined).
 */
public interface SqlTreeJoin {

  /**
   * Add the extra join if required.
   */
  void addJoin(DbSqlContext ctx);

}
