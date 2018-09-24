package io.ebean.config.dbplatform.sqlserver;

import io.ebean.BackgroundExecutor;
import io.ebean.Query;
import io.ebean.annotation.PersistBatch;
import io.ebean.annotation.Platform;
import io.ebean.config.PlatformConfig;
import io.ebean.config.dbplatform.DatabasePlatform;
import io.ebean.config.dbplatform.DbPlatformType;
import io.ebean.config.dbplatform.DbType;
import io.ebean.config.dbplatform.IdType;
import io.ebean.config.dbplatform.PlatformIdGenerator;
import io.ebean.config.dbplatform.SqlErrorCodes;

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
    this.persistBatchOnCascade = PersistBatch.NONE;
    this.idInExpandedForm = true;
    this.selectCountWithAlias = true;
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
    this.likeClauseRaw = "like ? collate Latin1_General_BIN";
    this.likeClauseEscaped = "like ? collate Latin1_General_BIN";

    booleanDbType = Types.INTEGER;
    this.dbDefaultValue.setFalse("0");
    this.dbDefaultValue.setTrue("1");
    this.dbDefaultValue.setNow("SYSUTCDATETIME()");
    dbTypeMap.put(DbType.BOOLEAN, new DbPlatformType("bit"));

    dbTypeMap.put(DbType.INTEGER, new DbPlatformType("integer", false));
    dbTypeMap.put(DbType.BIGINT, new DbPlatformType("numeric", 19));
    dbTypeMap.put(DbType.REAL, new DbPlatformType("float(16)"));
    dbTypeMap.put(DbType.DOUBLE, new DbPlatformType("float(32)"));
    dbTypeMap.put(DbType.TINYINT, new DbPlatformType("smallint"));
    dbTypeMap.put(DbType.DECIMAL, new DbPlatformType("numeric", 28));


    dbTypeMap.put(DbType.DATE, new DbPlatformType("date"));
    dbTypeMap.put(DbType.TIME, new DbPlatformType("time"));
    dbTypeMap.put(DbType.TIMESTAMP, new DbPlatformType("datetime2"));

    // UTF8 aware types - overwritten in SqlServer16 platform
    dbTypeMap.put(DbType.CHAR, new DbPlatformType("nchar", 1));
    dbTypeMap.put(DbType.VARCHAR, new DbPlatformType("nvarchar", 255));
    dbTypeMap.put(DbType.LONGVARCHAR, new DbPlatformType("nvarchar", Integer.MAX_VALUE));
    dbTypeMap.put(DbType.CLOB, new DbPlatformType("nvarchar", Integer.MAX_VALUE));

    dbTypeMap.put(DbType.JSON, new DbPlatformType("nvarchar", Integer.MAX_VALUE));
    dbTypeMap.put(DbType.JSONB, new DbPlatformType("nvarchar", Integer.MAX_VALUE));

    dbTypeMap.put(DbType.BLOB, new DbPlatformType("image"));
    dbTypeMap.put(DbType.LONGVARBINARY, new DbPlatformType("image"));
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
  public String fromForUpdate(Query.ForUpdate forUpdateMode) {
    switch (forUpdateMode) {
      case SKIPLOCKED:
        return "with (updlock,readpast)";
      case NOWAIT:
        return "with (updlock,nowait)";
      default:
        return "with (updlock)";
    }
  }

  @Override
  protected String withForUpdate(String sql, Query.ForUpdate forUpdateMode) {
    // for update are hints on from clause of base table
    return sql;
  }
}
