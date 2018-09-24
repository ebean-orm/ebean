package io.ebeaninternal.dbmigration.ddlgeneration.platform;

import io.ebean.config.dbplatform.DatabasePlatform;
import io.ebeaninternal.dbmigration.ddlgeneration.DdlBuffer;

import java.io.IOException;

/**
 * Postgres specific DDL.
 */
public class PostgresDdl extends PlatformDdl {

  public PostgresDdl(DatabasePlatform platform) {
    super(platform);
    this.historyDdl = new PostgresHistoryDdl();
    this.dropTableCascade = " cascade";
    this.columnSetType = "type ";
    this.alterTableIfExists = "if exists ";
    this.columnSetNull = "drop not null";
  }

  @Override
  public boolean suppressPrimaryKeyOnPartition() {
    return true;
  }

  @Override
  protected String convertArrayType(String logicalArrayType) {
    return NativeDbArray.logicalToNative(logicalArrayType);
  }

  /**
   * Map bigint, integer and smallint into their equivalent serial types.
   */
  @Override
  public String asIdentityColumn(String columnDefn) {

    if ("bigint".equalsIgnoreCase(columnDefn)) {
      return "bigserial";
    }
    if ("integer".equalsIgnoreCase(columnDefn)) {
      return "serial";
    }
    if ("smallint".equalsIgnoreCase(columnDefn)) {
      return "smallserial";
    }
    return columnDefn;
  }

  @Override
  public void addTablePartition(DdlBuffer apply, String partitionMode, String partitionColumn) throws IOException {
    apply.append(" partition by range (").append(partitionColumn).append(")");
  }
}
