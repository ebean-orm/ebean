package io.ebean.platform.sqlserver;

import io.ebean.BackgroundExecutor;
import io.ebean.Query;
import io.ebean.annotation.PersistBatch;
import io.ebean.annotation.Platform;
import io.ebean.config.PlatformConfig;
import io.ebean.config.dbplatform.*;

import javax.sql.DataSource;
import java.sql.Types;

/**
 * Base Microsoft SQL Server platform - NVarchar UTF types and Sequence preference by default.
 */
abstract class SqlServerBasePlatform extends DatabasePlatform {

  SqlServerBasePlatform() {
    super();
    this.platform = Platform.SQLSERVER;
    // disable persistBatchOnCascade mode for
    // SQL Server unless we are using sequences
    this.dbEncrypt = new SqlServerDbEncrypt();
    this.persistBatchOnCascade = PersistBatch.NONE;
    this.maxInBinding = 2000;
    this.idInExpandedForm = true;
    this.selectCountWithAlias = true;
    this.selectCountWithColumnAlias = true;
    this.sqlLimiter = new SqlServerSqlLimiter();
    this.basicSqlLimiter = new SqlServerBasicSqlLimiter();
    this.historySupport = new SqlServerHistorySupport();
    this.dbIdentity.setIdType(IdType.SEQUENCE);
    this.dbIdentity.setSupportsGetGeneratedKeys(true);
    this.dbIdentity.setSupportsIdentity(true);
    this.dbIdentity.setSupportsSequence(true);
    this.sequenceBatchMode = false;

    this.exceptionTranslator =
      new SqlErrorCodes()
        .addAcquireLock("1222")
        .addDuplicateKey("2601", "2627")
        .addDataIntegrity("544", "547", "8114", "8115")
        .build();

    this.openQuote = "[";
    this.closeQuote = "]";
    this.likeSpecialCharacters = new char[]{'%', '_', '['};
    this.likeClauseRaw = "like ?";
    this.likeClauseEscaped = "like ?";

    booleanDbType = Types.INTEGER;
    this.dbDefaultValue.setFalse("0");
    this.dbDefaultValue.setTrue("1");
    this.dbDefaultValue.setNow("SYSUTCDATETIME()");
    dbTypeMap.put(DbType.BOOLEAN, new DbPlatformType("bit"));
    dbTypeMap.put(DbType.INTEGER, new DbPlatformType("integer", false));
    dbTypeMap.put(DbType.BIGINT, new DbPlatformType("bigint", false));
    dbTypeMap.put(DbType.REAL, new DbPlatformType("float(16)"));
    dbTypeMap.put(DbType.DOUBLE, new DbPlatformType("float(32)"));
    dbTypeMap.put(DbType.TINYINT, new DbPlatformType("smallint"));
    dbTypeMap.put(DbType.DECIMAL, new DbPlatformType("numeric", 16, 3));
    dbTypeMap.put(DbType.DATE, new DbPlatformType("date"));
    dbTypeMap.put(DbType.TIME, new DbPlatformType("time"));
    dbTypeMap.put(DbType.TIMESTAMP, new DbPlatformType("datetime2"));
    dbTypeMap.put(DbType.LOCALDATETIME, new DbPlatformType("datetime2"));
    // UTF8 aware types - overwritten in SqlServer16 platform
    dbTypeMap.put(DbType.CHAR, new DbPlatformType("nchar", 1));

    DbPlatformType longVarchar = new DbPlatformType("nvarchar(max)", false);
    DbPlatformType varchar = new DbPlatformType("nvarchar", 255, 4000, longVarchar);
    DbPlatformType json = new DbPlatformType("nvarchar(max)", 0, varchar);
    dbTypeMap.put(DbType.VARCHAR, varchar);
    dbTypeMap.put(DbType.LONGVARCHAR, longVarchar);
    dbTypeMap.put(DbType.CLOB, longVarchar);
    dbTypeMap.put(DbType.JSON, json);
    dbTypeMap.put(DbType.JSONB, json);

    DbPlatformType blob = new DbPlatformType("varbinary(max)", false);
    dbTypeMap.put(DbType.VARBINARY, new DbPlatformType("varbinary", 255, 8000, blob));

    dbTypeMap.put(DbType.BLOB, blob);
    dbTypeMap.put(DbType.LONGVARBINARY, blob);
  }

  @Override
  public void configure(PlatformConfig config) {
    super.configure(config);
    if (dbIdentity.getIdType() == IdType.SEQUENCE) {
      this.persistBatchOnCascade = PersistBatch.ALL;
    }
  }

  @Override
  protected void escapeLikeCharacter(char ch, StringBuilder sb) {
    sb.append('[').append(ch).append(']');
  }

  /**
   * Create a Postgres specific sequence IdGenerator.
   */
  @Override
  public PlatformIdGenerator createSequenceIdGenerator(BackgroundExecutor be, DataSource ds, int stepSize, String seqName) {
    return new SqlServerStepSequence(be, ds, seqName, stepSize);
  }

  /**
   * For update is part of the FROM clause on the base table for sql server.
   */
  @Override
  public String fromForUpdate(Query.LockWait lockWait) {
    switch (lockWait) {
      case SKIPLOCKED:
        return "with (updlock,readpast)";
      case NOWAIT:
        return "with (updlock,nowait)";
      default:
        return "with (updlock)";
    }
  }

  @Override
  protected String withForUpdate(String sql, Query.LockWait lockWait, Query.LockType lockType) {
    // for update are hints on from clause of base table
    return sql;
  }

  @Override
  public boolean useMigrationStoredProcedures() {
    return true;
  }
}
