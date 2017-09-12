package io.ebean.config.dbplatform.oracle;

import io.ebean.BackgroundExecutor;
import io.ebean.Platform;
import io.ebean.config.CurrentTenantProvider;
import io.ebean.config.TenantDataSourceProvider;
import io.ebean.Query;
import io.ebean.config.dbplatform.BasicSqlAnsiLimiter;
import io.ebean.config.dbplatform.DatabasePlatform;
import io.ebean.config.dbplatform.DbPlatformType;
import io.ebean.config.dbplatform.DbType;
import io.ebean.config.dbplatform.IdType;
import io.ebean.config.dbplatform.PlatformIdGenerator;
import io.ebean.config.dbplatform.RownumSqlLimiter;
import io.ebean.dbmigration.ddlgeneration.platform.Oracle10Ddl;

import java.sql.Types;

/**
 * Oracle10 and greater specific platform.
 */
public class OraclePlatform extends DatabasePlatform {

  public OraclePlatform() {
    super();
    this.platform = Platform.ORACLE;
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

    this.dbDefaultValue.setFalse("0");
    this.dbDefaultValue.setTrue("1");
    this.dbDefaultValue.setNow("sysdate");

    this.treatEmptyStringsAsNull = true;

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
    dbTypeMap.put(DbType.DECIMAL, new DbPlatformType("number", 38));

    dbTypeMap.put(DbType.VARCHAR, new DbPlatformType("varchar2", 255));

    dbTypeMap.put(DbType.LONGVARBINARY, new DbPlatformType("blob"));
    dbTypeMap.put(DbType.LONGVARCHAR, new DbPlatformType("clob"));
    dbTypeMap.put(DbType.VARBINARY, new DbPlatformType("raw", 255));
    dbTypeMap.put(DbType.BINARY, new DbPlatformType("raw", 255));

    dbTypeMap.put(DbType.TIME, new DbPlatformType("timestamp"));
  }

  @Override
  public PlatformIdGenerator createSequenceIdGenerator(BackgroundExecutor be,
      TenantDataSourceProvider ds, String seqName, int batchSize, CurrentTenantProvider currentTenantProvider) {

    return new OracleSequenceIdGenerator(be, ds, seqName, batchSize, currentTenantProvider);
  }

  @Override
  protected String withForUpdate(String sql, Query.ForUpdate forUpdateMode) {
    switch (forUpdateMode) {
      case SKIPLOCKED:
        return sql + " for update skip locked";
      case NOWAIT:
        return sql + " for update nowait";
      default:
        return sql + " for update";
    }
  }
}
