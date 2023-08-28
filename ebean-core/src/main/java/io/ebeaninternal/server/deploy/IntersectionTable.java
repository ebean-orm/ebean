package io.ebeaninternal.server.deploy;

import io.ebean.Database;
import io.ebean.SqlUpdate;

public final class IntersectionTable {

  private final String insertSql;
  private final String deleteSql;

  IntersectionTable(String insertSql, String deleteSql) {
    this.insertSql = insertSql;
    this.deleteSql = deleteSql;
  }

  /**
   * Return a SqlUpdate for inserting into the intersection table.
   */
  public SqlUpdate insert(Database server) {
    return server.sqlUpdate(insertSql);
  }

  /**
   * Return a SqlUpdate for deleting from the intersection table.
   */
  public SqlUpdate delete(Database server) {
    return server.sqlUpdate(deleteSql);
  }

}
