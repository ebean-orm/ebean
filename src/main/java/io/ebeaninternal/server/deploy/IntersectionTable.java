package io.ebeaninternal.server.deploy;

import io.ebean.EbeanServer;
import io.ebean.SqlUpdate;

public class IntersectionTable {

  private final String insertSql;
  private final String deleteSql;

  IntersectionTable(String insertSql, String deleteSql) {
    this.insertSql = insertSql;
    this.deleteSql = deleteSql;
  }

  /**
   * Return a SqlUpdate for inserting into the intersection table.
   */
  public SqlUpdate insert(EbeanServer server) {
    return server.createSqlUpdate(insertSql);
  }

  /**
   * Return a SqlUpdate for deleting from the intersection table.
   */
  public SqlUpdate delete(EbeanServer server) {
    return server.createSqlUpdate(deleteSql);
  }

}
