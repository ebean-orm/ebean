package io.ebean.platform.oracle;

import io.ebean.BackgroundExecutor;
import io.ebean.Query;
import io.ebean.annotation.Platform;
import io.ebean.config.dbplatform.*;

import javax.sql.DataSource;
import java.sql.Types;

/**
 * Oracle specific platform.
 */
public class OraclePlatform extends DatabasePlatform {

  public OraclePlatform() {
    super();
    this.platform = Platform.ORACLE;
    this.supportsDeleteTableAlias = true;
    this.maxInBinding = 1000;
    this.maxTableNameLength = 30;
    this.maxConstraintNameLength = 30;
    this.dbEncrypt = new OracleDbEncrypt();
    this.sqlLimiter = new OracleAnsiSqlRowsLimiter();
    this.basicSqlLimiter = new BasicSqlAnsiLimiter();
    this.historySupport = new OracleDbHistorySupport();
    this.truncateTable = "truncate table %s cascade";
    dbIdentity.setIdType(IdType.IDENTITY);
    dbIdentity.setSupportsSequence(true);
    dbIdentity.setSupportsIdentity(true);
    dbIdentity.setSupportsGetGeneratedKeys(true);
    this.dbDefaultValue.setFalse("0");
    this.dbDefaultValue.setTrue("1");
    this.dbDefaultValue.setNow("current_timestamp");
    this.likeClauseRaw = "like ?";

    this.exceptionTranslator =
      new SqlErrorCodes()
        //.addAcquireLock("")
        .addDuplicateKey("1")
        .addDataIntegrity("2291")
        .addSerializableConflict("72000")
        .build();

    this.openQuote = "\"";
    this.closeQuote = "\"";

    booleanDbType = Types.INTEGER;
    dbTypeMap.put(DbType.BOOLEAN, new DbPlatformType("number(1)"));
    dbTypeMap.put(DbType.INTEGER, new DbPlatformType("number", 10));
    dbTypeMap.put(DbType.BIGINT, new DbPlatformType("number", 19));
    dbTypeMap.put(DbType.REAL, new DbPlatformType("number", 19, 4));
    dbTypeMap.put(DbType.DOUBLE, new DbPlatformType("number", 19, 4));
    dbTypeMap.put(DbType.SMALLINT, new DbPlatformType("number", 5));
    dbTypeMap.put(DbType.TINYINT, new DbPlatformType("number", 3));
    dbTypeMap.put(DbType.DECIMAL, new DbPlatformType("number", 16, 3));
    dbTypeMap.put(DbType.TIME, new DbPlatformType("timestamp"));

    DbPlatformType blob = new DbPlatformType("blob", false);
    dbTypeMap.put(DbType.BLOB, blob);
    dbTypeMap.put(DbType.LONGVARBINARY, blob);
    dbTypeMap.put(DbType.VARBINARY, new DbPlatformType("raw", 255, 2000, blob));
    dbTypeMap.put(DbType.BINARY, new DbPlatformType("raw", 255, 2000, blob));

    DbPlatformType clob = new DbPlatformType("clob", false);
    dbTypeMap.put(DbType.LONGVARCHAR, clob);
    dbTypeMap.put(DbType.VARCHAR, new DbPlatformType("varchar2", 255, 4000, clob));
  }

  @Override
  public PlatformIdGenerator createSequenceIdGenerator(BackgroundExecutor be, DataSource ds, int stepSize, String seqName) {
    return new OracleSequenceIdGenerator(be, ds, seqName, sequenceBatchSize);
  }

  @Override
  protected String withForUpdate(String sql, Query.LockWait lockWait, Query.LockType lockType) {
    switch (lockWait) {
      case SKIPLOCKED:
        return sql + " for update skip locked";
      case NOWAIT:
        return sql + " for update nowait";
      default:
        return sql + " for update";
    }
  }

}
