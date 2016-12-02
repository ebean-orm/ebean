package com.avaje.ebean.config.dbplatform;

import java.sql.Types;

import com.avaje.ebean.config.PersistBatch;
import com.avaje.ebean.config.Platform;
import com.avaje.ebean.dbmigration.ddlgeneration.platform.MsSqlServerDdl;

/**
 * Microsoft SQL Server 2012 specific platform.
 * <p>
 * <ul>
 * <li>supportsGetGeneratedKeys = true</li>
 * <li>Uses LIMIT OFFSET clause</li>
 * <li>Uses [ & ] for quoted identifiers</li>
 * </ul>
 * </p>
 */
public class MsSqlServer2012Platform extends DatabasePlatform {

  public MsSqlServer2012Platform() {
    super();
    this.name = Platform.SQLSERVER.name().toLowerCase();
    // FIXME Refactor to store Platform enum directly. AnnotationBase does the other way round:
    // this.platform = Platform.valueOf(databasePlatform.getName().toUpperCase());


    // effectively disable persistBatchOnCascade mode for SQL Server
    // due to lack of support for getGeneratedKeys in batch mode
    this.persistBatchOnCascade = PersistBatch.NONE;
    // enable DbViewHistorySupport - warning untested !
    this.historySupport = new MsSqlServer2012HistorySupport();
    this.idInExpandedForm = true;
    this.selectCountWithAlias = true;
    // uses ORDER BY OFFSET GET NEXT ROWS enhancement introduced in MS SQL Server 2012
    this.basicSqlLimiter = new BasicMsSqlLimiter();
    // FIXME: should be reworked to also use the built-in support in MS SQL Server 2012
    this.sqlLimiter = new MsSqlServer2005SqlLimiter();
    this.platformDdl = new MsSqlServerDdl(this);
    this.dbIdentity.setIdType(IdType.IDENTITY);
    this.dbIdentity.setSupportsGetGeneratedKeys(true);
    this.dbIdentity.setSupportsIdentity(true);

    this.openQuote = "[";
    this.closeQuote = "]";
    this.booleanDbType = Types.BIT; // solves problems with unqoted false/true

    dbTypeMap.put(DbType.BIT, new DbPlatformType("bit default 0"));
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

  @Override
  public boolean needsIdentityInsert() {
    return true;
  }
}
