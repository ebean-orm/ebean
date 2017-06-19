package io.ebean.config.dbplatform.sqlserver;

import io.ebean.BackgroundExecutor;
import io.ebean.Platform;
import io.ebean.config.CurrentTenantProvider;
import io.ebean.config.TenantDataSourceProvider;
import io.ebean.config.dbplatform.DatabasePlatform;
import io.ebean.config.dbplatform.DbPlatformType;
import io.ebean.config.dbplatform.DbType;
import io.ebean.config.dbplatform.IdType;
import io.ebean.config.dbplatform.PlatformIdGenerator;
import io.ebean.config.dbplatform.SqlErrorCodes;
import io.ebean.dbmigration.ddlgeneration.platform.SqlServerDdl;

import java.sql.Types;

/**
 * Microsoft SQL Server platform.
 */
public class SqlServerPlatform extends DatabasePlatform {

  public SqlServerPlatform() {
    super();
    this.platform = Platform.SQLSERVER;
    this.idInExpandedForm = true;
    this.selectCountWithAlias = true;
    this.sqlLimiter = new SqlServerSqlLimiter();
    this.basicSqlLimiter = new SqlServerBasicSqlLimiter();
    this.platformDdl = new SqlServerDdl(this);
    this.historySupport = new SqlServerHistorySupport();
    dbIdentity.setIdType(IdType.SEQUENCE);
    // Not using getGeneratedKeys as instead we will
    // batch load sequences which enables JDBC batch execution
    dbIdentity.setSupportsGetGeneratedKeys(false);
    dbIdentity.setSupportsSequence(true);

    this.exceptionTranslator =
      new SqlErrorCodes()
        .addAcquireLock("1222")
        .addDuplicateKey("2601","2627","23000")
        .addDataIntegrity("544","8114","8115")
        .build();

    this.openQuote = "[";
    this.closeQuote = "]";

    booleanDbType = Types.INTEGER;
    dbTypeMap.put(DbType.BOOLEAN, new DbPlatformType("bit default 0"));

    dbTypeMap.put(DbType.INTEGER, new DbPlatformType("integer", false));
    dbTypeMap.put(DbType.BIGINT, new DbPlatformType("numeric", 19));
    dbTypeMap.put(DbType.REAL, new DbPlatformType("float(16)"));
    dbTypeMap.put(DbType.DOUBLE, new DbPlatformType("float(32)"));
    dbTypeMap.put(DbType.TINYINT, new DbPlatformType("smallint"));
    dbTypeMap.put(DbType.DECIMAL, new DbPlatformType("numeric", 28));

    dbTypeMap.put(DbType.BLOB, new DbPlatformType("image"));
    dbTypeMap.put(DbType.CLOB, new DbPlatformType("text"));
    dbTypeMap.put(DbType.LONGVARBINARY, new DbPlatformType("image"));
    dbTypeMap.put(DbType.LONGVARCHAR, new DbPlatformType("text"));

    dbTypeMap.put(DbType.DATE, new DbPlatformType("date"));
    dbTypeMap.put(DbType.TIME, new DbPlatformType("time"));
    dbTypeMap.put(DbType.TIMESTAMP, new DbPlatformType("datetime2"));

  }

  /**
   * Create a SqlServer specific sequence IdGenerator.
   */
  @Override
  public PlatformIdGenerator createSequenceIdGenerator(BackgroundExecutor be,
      TenantDataSourceProvider ds, String seqName, int batchSize, CurrentTenantProvider currentTenantProvider) {

    return new SqlServerSequenceIdGenerator(be, ds, seqName, batchSize, currentTenantProvider);
  }

}
