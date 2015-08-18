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
    // OnQueryOnly.CLOSE as a performance optimisation on Oracle
    this.onQueryOnly = OnQueryOnly.CLOSE;
    this.dbEncrypt = new OracleDbEncrypt();
    this.sqlLimiter = new RownumSqlLimiter();
    this.platformDdl = new Oracle10Ddl(this.dbTypeMap, this.dbIdentity);
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
    dbTypeMap.put(Types.BOOLEAN, new DbType("number(1) default 0"));

    dbTypeMap.put(Types.INTEGER, new DbType("number", 10));
    dbTypeMap.put(Types.BIGINT, new DbType("number", 19));
    dbTypeMap.put(Types.REAL, new DbType("number", 19, 4));
    dbTypeMap.put(Types.DOUBLE, new DbType("number", 19, 4));
    dbTypeMap.put(Types.SMALLINT, new DbType("number", 5));
    dbTypeMap.put(Types.TINYINT, new DbType("number", 3));
    dbTypeMap.put(Types.DECIMAL, new DbType("number", 38));

    dbTypeMap.put(Types.VARCHAR, new DbType("varchar2", 255));

    dbTypeMap.put(Types.LONGVARBINARY, new DbType("blob"));
    dbTypeMap.put(Types.LONGVARCHAR, new DbType("clob"));
    dbTypeMap.put(Types.VARBINARY, new DbType("raw", 255));
    dbTypeMap.put(Types.BINARY, new DbType("raw", 255));

    dbTypeMap.put(Types.TIME, new DbType("timestamp"));
  }

  @Override
  public IdGenerator createSequenceIdGenerator(BackgroundExecutor be, DataSource ds, String seqName, int batchSize) {

    return new OracleSequenceIdGenerator(be, ds, seqName, batchSize);
  }

  @Override
  protected String withForUpdate(String sql) {
    return sql + " for update";
  }
}
