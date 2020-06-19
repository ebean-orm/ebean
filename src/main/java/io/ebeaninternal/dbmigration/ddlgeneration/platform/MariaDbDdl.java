package io.ebeaninternal.dbmigration.ddlgeneration.platform;

import io.ebean.config.dbplatform.DatabasePlatform;

/**
 * MariaDB platform DDL.
 */
public class MariaDbDdl extends MySqlDdl {

  public MariaDbDdl(DatabasePlatform platform) {
    super(platform);
    this.historyDdl = new MariaDbHistoryDdl();
  }
}
