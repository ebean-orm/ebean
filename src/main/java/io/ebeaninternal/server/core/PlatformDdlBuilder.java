package io.ebeaninternal.server.core;

import io.ebean.config.dbplatform.DatabasePlatform;
import io.ebeaninternal.dbmigration.ddlgeneration.platform.CockroachDdl;
import io.ebeaninternal.dbmigration.ddlgeneration.platform.DB2Ddl;
import io.ebeaninternal.dbmigration.ddlgeneration.platform.H2Ddl;
import io.ebeaninternal.dbmigration.ddlgeneration.platform.HsqldbDdl;
import io.ebeaninternal.dbmigration.ddlgeneration.platform.MySqlDdl;
import io.ebeaninternal.dbmigration.ddlgeneration.platform.Oracle10Ddl;
import io.ebeaninternal.dbmigration.ddlgeneration.platform.PlatformDdl;
import io.ebeaninternal.dbmigration.ddlgeneration.platform.PostgresDdl;
import io.ebeaninternal.dbmigration.ddlgeneration.platform.SQLiteDdl;
import io.ebeaninternal.dbmigration.ddlgeneration.platform.SqlServerDdl;

/**
 * Builds platform specific DDL handler.
 */
public class PlatformDdlBuilder {

  /**
   * Return platform specific DDL handler.
   */
  public static PlatformDdl create(DatabasePlatform platform) {

    switch (platform.getPlatform()) {
      case H2:
        return new H2Ddl(platform);
      case DB2:
        return new DB2Ddl(platform);
      case MYSQL:
        return new MySqlDdl(platform);
      case HSQLDB:
        return new HsqldbDdl(platform);
      case ORACLE:
        return new Oracle10Ddl(platform);
      case SQLITE:
        return new SQLiteDdl(platform);
      case GENERIC:
        return new PlatformDdl(platform);
      case POSTGRES:
        return new PostgresDdl(platform);
      case COCKROACH:
        return new CockroachDdl(platform);
      case SQLANYWHERE:
        return new PlatformDdl(platform);
      case SQLSERVER16:
      case SQLSERVER17:
      case SQLSERVER:
        return new SqlServerDdl(platform);
      default:
        return new PlatformDdl(platform);
    }
  }
}
