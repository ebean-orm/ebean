package io.ebean.config.dbplatform.sqlserver;

import io.ebean.PersistBatch;
import io.ebean.DuplicateKeyException;
import io.ebean.Platform;
import io.ebean.Query.ForUpdate;
import io.ebean.config.dbplatform.DatabasePlatform;
import io.ebean.config.dbplatform.DbPlatformType;
import io.ebean.config.dbplatform.DbType;
import io.ebean.config.dbplatform.IdType;
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
    // effectively disable persistBatchOnCascade mode for SQL Server
    // due to lack of support for getGeneratedKeys in batch mode
    this.persistBatchOnCascade = PersistBatch.NONE;
    this.idInExpandedForm = true;
    this.selectCountWithAlias = true;
    this.sqlLimiter = new SqlServerSqlLimiter();
    this.basicSqlLimiter = new SqlServerBasicSqlLimiter();
    this.platformDdl = new SqlServerDdl(this);
    this.historySupport = new SqlServerHistorySupport();
    this.dbIdentity.setIdType(IdType.IDENTITY);
    this.dbIdentity.setSupportsGetGeneratedKeys(true);
    this.dbIdentity.setSupportsIdentity(true);

    this.exceptionTranslator =
      new SqlErrorCodes()
        .addAcquireLock("1222")
        .addDuplicateKey("2601", "2627")
        .addDataIntegrity("544", "8114", "8115")
        .build();

    this.openQuote = "[";
    this.closeQuote = "]";
    this.specialLikeCharacters = new char[]{'%', '_', '['};
    this.likeClause = "like ? COLLATE Latin1_General_BIN";

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

    dbTypeMap.put(DbType.JSON, new DbPlatformType("nvarchar", Integer.MAX_VALUE));
    dbTypeMap.put(DbType.JSONB, new DbPlatformType("nvarchar", Integer.MAX_VALUE));

  }

  @Override
  protected void escapeLikeCharacter(char ch, StringBuilder sb) {
    sb.append('[').append(ch).append(']');
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
