package com.avaje.ebean.config.dbplatform;

import com.avaje.ebean.BackgroundExecutor;
import com.avaje.ebean.dbmigration.ddlgeneration.platform.Oracle10Ddl;

import javax.sql.DataSource;
import java.sql.Types;

/**
 * Oracle10 and greater specific platform.
 */
public class OraclePlatform extends DatabasePlatform {

  public OraclePlatform() {
    super();
    this.name = "oracle";
    this.maxTableNameLength = 30;
    this.maxConstraintNameLength = 30;
    this.dbEncrypt = new OracleDbEncrypt();
    this.sqlLimiter = new RownumSqlLimiter();
    this.basicSqlLimiter = new BasicSqlAnsiLimiter();
    this.platformDdl = new Oracle10Ddl(this);
    this.historySupport = new OracleDbHistorySupport();

    // Not using getGeneratedKeys as instead we will
    // batch load sequences which enables JDBC batch execution
    dbIdentity.setSupportsGetGeneratedKeys(false);
    dbIdentity.setIdType(IdType.SEQUENCE);
    dbIdentity.setSupportsSequence(true);

    this.treatEmptyStringsAsNull = true;

    this.openQuote = "\"";
    this.closeQuote = "\"";

    booleanDbType = Types.INTEGER;
    dbTypeMap.put(DbType.BOOLEAN, new DbPlatformType("number(1) default 0"));

    dbTypeMap.put(DbType.INTEGER, new DbPlatformType("number", 10));
    dbTypeMap.put(DbType.BIGINT, new DbPlatformType("number", 19));
    dbTypeMap.put(DbType.REAL, new DbPlatformType("number", 19, 4));
    dbTypeMap.put(DbType.DOUBLE, new DbPlatformType("number", 19, 4));
    dbTypeMap.put(DbType.SMALLINT, new DbPlatformType("number", 5));
    dbTypeMap.put(DbType.TINYINT, new DbPlatformType("number", 3));
    dbTypeMap.put(DbType.DECIMAL, new DbPlatformType("number", 38));

    dbTypeMap.put(DbType.VARCHAR, new DbPlatformType("varchar2", 255));

    dbTypeMap.put(DbType.LONGVARBINARY, new DbPlatformType("blob"));
    dbTypeMap.put(DbType.LONGVARCHAR, new DbPlatformType("clob"));
    dbTypeMap.put(DbType.VARBINARY, new DbPlatformType("raw", 255));
    dbTypeMap.put(DbType.BINARY, new DbPlatformType("raw", 255));

    dbTypeMap.put(DbType.TIME, new DbPlatformType("timestamp"));
  }

  @Override
  public PlatformIdGenerator createSequenceIdGenerator(BackgroundExecutor be, DataSource ds, String seqName, int batchSize) {

    return new OracleSequenceIdGenerator(be, ds, seqName, batchSize);
  }

  @Override
  protected String withForUpdate(String sql) {
    return sql + " for update";
  }
}
