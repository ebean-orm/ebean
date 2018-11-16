package io.ebeaninternal.server.deploy;

import io.ebean.EbeanServer;
import io.ebean.SqlUpdate;

public class IntersectionTable {

  private final String insertSql;
  private final String deleteSql;
  private final String draftInsertSql;
  private final String draftDeleteSql;

  IntersectionTable(String insertSql, String deleteSql, String draftInsertSql, String draftDeleteSql) {
    this.insertSql = insertSql;
    this.deleteSql = deleteSql;
    this.draftInsertSql = draftInsertSql;
    this.draftDeleteSql = draftDeleteSql;
  }

  /**
   * Return a SqlUpdate for inserting into the intersection table.
   */
  public SqlUpdate insert(EbeanServer server, boolean draft) {
    return server.createSqlUpdate(draft ? draftInsertSql : insertSql);
  }

  /**
   * Return a SqlUpdate for deleting from the intersection table.
   */
  public SqlUpdate delete(EbeanServer server, boolean draft) {
    return server.createSqlUpdate(draft ? draftDeleteSql : deleteSql);
  }

}
