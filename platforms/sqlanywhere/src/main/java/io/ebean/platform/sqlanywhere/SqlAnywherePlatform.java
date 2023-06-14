package io.ebean.platform.sqlanywhere;

import io.ebean.annotation.Platform;
import io.ebean.config.dbplatform.DatabasePlatform;
import io.ebean.config.dbplatform.DbPlatformType;
import io.ebean.config.dbplatform.DbType;
import io.ebean.config.dbplatform.IdType;

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
    this.platform = Platform.SQLANYWHERE;
    this.dbIdentity.setIdType(IdType.IDENTITY);
    this.likeClauseRaw = "like ?";
    this.sqlLimiter = new SqlAnywhereLimiter();
    this.dbIdentity.setSupportsGetGeneratedKeys(false);
    this.dbIdentity.setSelectLastInsertedIdTemplate("select @@IDENTITY as X");
    this.dbIdentity.setSupportsIdentity(true);

    dbTypeMap.put(DbType.BOOLEAN, new DbPlatformType("bit"));
    dbTypeMap.put(DbType.BIGINT, new DbPlatformType("numeric", 19));
    dbTypeMap.put(DbType.REAL, new DbPlatformType("float(16)"));
    dbTypeMap.put(DbType.DOUBLE, new DbPlatformType("float(32)"));
    dbTypeMap.put(DbType.TINYINT, new DbPlatformType("smallint"));
    dbTypeMap.put(DbType.DECIMAL, new DbPlatformType("numeric", 16, 3));

    DbPlatformType clob = new DbPlatformType("long varchar", false);
    dbTypeMap.put(DbType.CLOB, clob);
    dbTypeMap.put(DbType.LONGVARCHAR,clob);
    dbTypeMap.put(DbType.VARCHAR, new DbPlatformType("varchar", 255, 32767, clob));

    DbPlatformType blob = new DbPlatformType("long binary", false);
    dbTypeMap.put(DbType.VARBINARY, new DbPlatformType("varbinary", 255, 32767, blob));
    dbTypeMap.put(DbType.LONGVARBINARY, blob);
    dbTypeMap.put(DbType.BLOB, blob);


  }

}
