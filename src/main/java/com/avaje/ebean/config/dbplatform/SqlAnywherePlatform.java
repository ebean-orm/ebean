package com.avaje.ebean.config.dbplatform;

import java.sql.Types;

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

    dbTypeMap.put(Types.BOOLEAN, new DbType("bit default 0"));
    dbTypeMap.put(Types.BIGINT, new DbType("numeric", 19));
    dbTypeMap.put(Types.REAL, new DbType("float(16)"));
    dbTypeMap.put(Types.DOUBLE, new DbType("float(32)"));
    dbTypeMap.put(Types.TINYINT, new DbType("smallint"));
    dbTypeMap.put(Types.DECIMAL, new DbType("numeric", 28));

    dbTypeMap.put(Types.BLOB, new DbType("binary(4500)"));
    dbTypeMap.put(Types.CLOB, new DbType("long varchar"));
    dbTypeMap.put(Types.LONGVARBINARY, new DbType("long binary"));
    dbTypeMap.put(Types.LONGVARCHAR, new DbType("long varchar"));

  }

}
