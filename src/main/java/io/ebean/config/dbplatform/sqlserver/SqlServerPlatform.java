package io.ebean.config.dbplatform.sqlserver;

import io.ebean.BackgroundExecutor;
import io.ebean.DuplicateKeyException;
import io.ebean.Platform;
import io.ebean.Query.ForUpdate;
import io.ebean.config.CurrentTenantProvider;
import io.ebean.config.TenantDataSourceProvider;
import io.ebean.config.dbplatform.DatabasePlatform;
import io.ebean.config.dbplatform.DbPlatformType;
import io.ebean.config.dbplatform.DbType;
import io.ebean.config.dbplatform.IdType;
import io.ebean.config.dbplatform.MultiValueMode;
import io.ebean.config.dbplatform.PlatformIdGenerator;
import io.ebean.config.dbplatform.SqlErrorCodes;
import io.ebean.dbmigration.ddlgeneration.platform.SqlServerDdl;

import java.sql.SQLException;
import java.sql.Types;
import java.util.regex.Pattern;

import javax.persistence.PersistenceException;

/**
 * Microsoft SQL Server platform.
 */
public class SqlServerPlatform extends DatabasePlatform {

  private static final Pattern FIRST_TABLE_ALIAS = Pattern.compile("\\st0\\s");
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
        //              mssql2005?                  mssla2016 w. microsoft jdbc (see SQLServerException class)
        .addAcquireLock("1222",                     "S00051")
        .addDuplicateKey("2601", "2627")            // unfortunately, mssql has the same error code for duplicate key & data integrity
        .addDataIntegrity("544", "8114", "8115",    "23000")
        .build();

    this.openQuote = "[";
    this.closeQuote = "]";
    this.specialLikeCharacters = new char[]{'%', '_', '['};
    this.likeClause = "like ? COLLATE Latin1_General_BIN";

    booleanDbType = Types.BIT;
    this.dbDefaultValue.setFalse("0");
    this.dbDefaultValue.setTrue("1");
    this.dbDefaultValue.setNow("SYSUTCDATETIME()");
    this.multiValueMode = MultiValueMode.SQLSERVER_TVP;
    
    dbTypeMap.put(DbType.BOOLEAN, new DbPlatformType("bit"));

    dbTypeMap.put(DbType.INTEGER, new DbPlatformType("integer", false));
    dbTypeMap.put(DbType.BIGINT, new DbPlatformType("numeric", 19));
    dbTypeMap.put(DbType.REAL, new DbPlatformType("float(16)"));
    dbTypeMap.put(DbType.DOUBLE, new DbPlatformType("float(32)"));
    dbTypeMap.put(DbType.TINYINT, new DbPlatformType("smallint"));
    dbTypeMap.put(DbType.DECIMAL, new DbPlatformType("numeric", 28));

    dbTypeMap.put(DbType.BLOB, new DbPlatformType("image"));
    dbTypeMap.put(DbType.CLOB, new DbPlatformType("nvarchar", Integer.MAX_VALUE));
    dbTypeMap.put(DbType.LONGVARBINARY, new DbPlatformType("image"));
    dbTypeMap.put(DbType.LONGVARCHAR, new DbPlatformType("nvarchar", Integer.MAX_VALUE));

    dbTypeMap.put(DbType.DATE, new DbPlatformType("date"));
    dbTypeMap.put(DbType.TIME, new DbPlatformType("time"));
    dbTypeMap.put(DbType.TIMESTAMP, new DbPlatformType("datetime2"));

    dbTypeMap.put(DbType.JSON, new DbPlatformType("nvarchar", Integer.MAX_VALUE));
    dbTypeMap.put(DbType.JSONB, new DbPlatformType("nvarchar", Integer.MAX_VALUE));

  }

  @Override
  protected void escapeLikeCharacter(char ch, StringBuilder sb) {
    sb.append('[').append(ch).append(']');
  }

  /**
   * Create a SqlServer specific sequence IdGenerator.
   */
  @Override
  public PlatformIdGenerator createSequenceIdGenerator(BackgroundExecutor be,
      TenantDataSourceProvider ds, String seqName, int batchSize, CurrentTenantProvider currentTenantProvider) {

    return new SqlServerSequenceIdGenerator(be, ds, seqName, batchSize, currentTenantProvider);
  }

  @Override
  protected String withForUpdate(String sql, ForUpdate forUpdateMode) {
    // Here we do a simple string replacement by replacing " t0 ", the first table alias with the table hint.
    // This seems to be the minimal invasive implementation for now.
    switch (forUpdateMode) {
    case NOWAIT:
      return FIRST_TABLE_ALIAS.matcher(sql).replaceFirst(" t0 with (updlock,nowait) ");
    case SKIPLOCKED:
      return FIRST_TABLE_ALIAS.matcher(sql).replaceFirst(" t0 with (updlock,readpast) ");
    case BASE:
      return FIRST_TABLE_ALIAS.matcher(sql).replaceFirst(" t0 with (updlock) ");
    default:
      return sql;
    }
   // return super.withForUpdate(sql, forUpdateMode);
  }

  @Override
  public PersistenceException translate(String message, SQLException e) {
    String cause = e.getMessage();
    if (cause != null  && "23000".equals(e.getSQLState()) && cause.contains(" duplicate key ")) { 
      return new DuplicateKeyException(message, e);
    } else {
      return super.translate(message, e);
    }
  }
}
