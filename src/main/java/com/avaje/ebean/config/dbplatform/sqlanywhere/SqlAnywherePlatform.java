package com.avaje.ebean.config.dbplatform.sqlanywhere;

import com.avaje.ebean.config.dbplatform.DatabasePlatform;
import com.avaje.ebean.config.dbplatform.DbPlatformType;
import com.avaje.ebean.config.dbplatform.DbType;
import com.avaje.ebean.config.dbplatform.IdType;

/**
 * Sybase SQL Anywhere specific platform.
 * <p>
 * <ul>
 * <li>supportsGetGeneratedKeys = false</li>
 * <li>Uses TOP START AT clause</li>
 * </ul>
 * </p>
 */
public class SqlAnywherePlatform extends DatabasePlatform {

  public SqlAnywherePlatform() {
    super();
    this.name = "sqlanywhere";
    this.dbIdentity.setIdType(IdType.IDENTITY);

    this.sqlLimiter = new SqlAnywhereLimiter();
    this.dbIdentity.setSupportsGetGeneratedKeys(false);
    this.dbIdentity.setSelectLastInsertedIdTemplate("select @@IDENTITY as X");
    this.dbIdentity.setSupportsIdentity(true);

    dbTypeMap.put(DbType.BOOLEAN, new DbPlatformType("bit default 0"));
    dbTypeMap.put(DbType.BIGINT, new DbPlatformType("numeric", 19));
    dbTypeMap.put(DbType.REAL, new DbPlatformType("float(16)"));
    dbTypeMap.put(DbType.DOUBLE, new DbPlatformType("float(32)"));
    dbTypeMap.put(DbType.TINYINT, new DbPlatformType("smallint"));
    dbTypeMap.put(DbType.DECIMAL, new DbPlatformType("numeric", 28));

    dbTypeMap.put(DbType.BLOB, new DbPlatformType("binary(4500)"));
    dbTypeMap.put(DbType.CLOB, new DbPlatformType("long varchar"));
    dbTypeMap.put(DbType.LONGVARBINARY, new DbPlatformType("long binary"));
    dbTypeMap.put(DbType.LONGVARCHAR, new DbPlatformType("long varchar"));

  }

}
