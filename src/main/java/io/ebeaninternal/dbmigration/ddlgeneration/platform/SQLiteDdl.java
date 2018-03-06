package io.ebeaninternal.dbmigration.ddlgeneration.platform;

import io.ebean.config.dbplatform.DatabasePlatform;
import io.ebeaninternal.dbmigration.ddlgeneration.DdlBuffer;

/**
 * DB2 platform specific DDL.
 */
public class SQLiteDdl extends PlatformDdl {

  public SQLiteDdl(DatabasePlatform platform) {
    super(platform);
    this.identitySuffix = "";
    this.inlineForeignKeys = true;
  }

  @Override
  public void addTableComment(DdlBuffer apply, String tableName, String tableComment) {
    // not supported
  }

  @Override
  public void addColumnComment(DdlBuffer apply, String table, String column, String comment) {
    // not supported
  }

  @Override
  public String alterTableAddForeignKey(WriteForeignKey request) {
    // not supported
    return null;
  }
}
