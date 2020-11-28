package io.ebeaninternal.dbmigration.ddlgeneration.platform;

import io.ebean.config.dbplatform.DatabasePlatform;

public class Postgres9Ddl extends PostgresDdl {

  public Postgres9Ddl(DatabasePlatform platform) {
    super(platform);
  }

  /**
   * Map bigint, integer and smallint into their equivalent serial types.
   */
  @Override
  public String asIdentityColumn(String columnDefn, DdlIdentity identity) {
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
}
