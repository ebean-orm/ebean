package io.ebeaninternal.dbmigration.ddlgeneration.platform;

import io.ebean.config.dbplatform.DatabasePlatform;
import io.ebeaninternal.dbmigration.ddlgeneration.DdlBuffer;

/**
 * CockroachDB specific DDL handling.
 */
public class CockroachDdl extends PlatformDdl {

  public CockroachDdl(DatabasePlatform platform) {
    super(platform);
    this.dropTableCascade = " cascade";
    this.columnSetType = "type";
    this.alterTableIfExists = "if exists ";
    this.columnSetNull = "drop not null";
  }

  @Override
  protected String convertArrayType(String logicalArrayType) {
    return NativeDbArray.logicalToNative(logicalArrayType);
  }

  @Override
  public String asIdentityColumn(String columnDefn, DdlIdentity identity) {
    return asIdentityStandardOptions(columnDefn, identity);
  }

  @Override
  public void addTableComment(DdlBuffer apply, String tableName, String tableComment) {
    // do nothing
  }

  @Override
  public void addColumnComment(DdlBuffer apply, String table, String column, String comment) {
    // do nothing
  }

  @Override
  public boolean isInlineComments() {
    return false;
  }
}
