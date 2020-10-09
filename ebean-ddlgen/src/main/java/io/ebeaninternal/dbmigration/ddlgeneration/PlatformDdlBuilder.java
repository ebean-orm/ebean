package io.ebeaninternal.dbmigration.ddlgeneration;

import io.ebean.config.dbplatform.DatabasePlatform;
import io.ebeaninternal.dbmigration.ddlgeneration.platform.ClickHouseDdl;
import io.ebeaninternal.dbmigration.ddlgeneration.platform.CockroachDdl;
import io.ebeaninternal.dbmigration.ddlgeneration.platform.DB2Ddl;
import io.ebeaninternal.dbmigration.ddlgeneration.platform.H2Ddl;
import io.ebeaninternal.dbmigration.ddlgeneration.platform.HanaColumnStoreDdl;
import io.ebeaninternal.dbmigration.ddlgeneration.platform.HsqldbDdl;
import io.ebeaninternal.dbmigration.ddlgeneration.platform.MariaDbDdl;
import io.ebeaninternal.dbmigration.ddlgeneration.platform.MySqlDdl;
import io.ebeaninternal.dbmigration.ddlgeneration.platform.NuoDbDdl;
import io.ebeaninternal.dbmigration.ddlgeneration.platform.OracleDdl;
import io.ebeaninternal.dbmigration.ddlgeneration.platform.PlatformDdl;
import io.ebeaninternal.dbmigration.ddlgeneration.platform.Postgres9Ddl;
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
      case MARIADB:
        return new MariaDbDdl(platform);
      case MYSQL55:
      case MYSQL:
        return new MySqlDdl(platform);
      case HSQLDB:
        return new HsqldbDdl(platform);
      case NUODB:
        return new NuoDbDdl(platform);
      case ORACLE:
      case ORACLE11:
        return new OracleDdl(platform);
      case SQLITE:
        return new SQLiteDdl(platform);
      case POSTGRES9:
        return new Postgres9Ddl(platform);
      case POSTGRES:
        return new PostgresDdl(platform);
      case COCKROACH:
        return new CockroachDdl(platform);
      case SQLSERVER16:
      case SQLSERVER17:
      case SQLSERVER:
        return new SqlServerDdl(platform);
      case HANA:
        return new HanaColumnStoreDdl(platform);
      case CLICKHOUSE:
        return new ClickHouseDdl(platform);
      default:
        return new PlatformDdl(platform);
    }
  }
}
