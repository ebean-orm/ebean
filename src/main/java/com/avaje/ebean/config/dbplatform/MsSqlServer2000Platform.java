package com.avaje.ebean.config.dbplatform;

import com.avaje.ebean.config.PersistBatch;

/**
 * Microsoft SQL Server 2000 specific platform.
 * <p>
 * <ul>
 * <li>supportsGetGeneratedKeys = false</li>
 * <li>Use select @@IDENTITY to return the generated Id instead</li>
 * <li>Uses LIMIT OFFSET clause</li>
 * <li>Uses [ & ] for quoted identifiers</li>
 * </ul>
 * </p>
 */
public class MsSqlServer2000Platform extends DatabasePlatform {

  public MsSqlServer2000Platform() {
    super();
    this.name = "mssqlserver2000";
    this.persistBatchOnCascade = PersistBatch.NONE;
    this.dbIdentity.setIdType(IdType.IDENTITY);
    this.dbIdentity.setSupportsGetGeneratedKeys(false);
    this.dbIdentity.setSelectLastInsertedIdTemplate("select @@IDENTITY as X");
    this.dbIdentity.setSupportsIdentity(true);

    this.openQuote = "[";
    this.closeQuote = "]";

    dbTypeMap.put(DbType.BOOLEAN, new DbPlatformType("bit default 0"));

    dbTypeMap.put(DbType.BIGINT, new DbPlatformType("numeric", 19));
    dbTypeMap.put(DbType.REAL, new DbPlatformType("float(16)"));
    dbTypeMap.put(DbType.DOUBLE, new DbPlatformType("float(32)"));
    dbTypeMap.put(DbType.TINYINT, new DbPlatformType("smallint"));
    dbTypeMap.put(DbType.DECIMAL, new DbPlatformType("numeric", 28));

    dbTypeMap.put(DbType.BLOB, new DbPlatformType("image"));
    dbTypeMap.put(DbType.CLOB, new DbPlatformType("text"));
    dbTypeMap.put(DbType.LONGVARBINARY, new DbPlatformType("image"));
    dbTypeMap.put(DbType.LONGVARCHAR, new DbPlatformType("text"));

    dbTypeMap.put(DbType.DATE, new DbPlatformType("datetime"));
    dbTypeMap.put(DbType.TIME, new DbPlatformType("datetime"));
    dbTypeMap.put(DbType.TIMESTAMP, new DbPlatformType("datetime"));

  }

}
