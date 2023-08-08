package io.ebean.platform.sqlserver;

import io.ebean.annotation.Platform;
import io.ebean.config.dbplatform.DbPlatformType;
import io.ebean.config.dbplatform.DbType;
import io.ebean.config.dbplatform.IdType;

/**
 * Microsoft SQL Server platform that has non-UTF8 types (char, varchar, text) and default to Identity rather than Sequence.
 */
public class SqlServer16Platform extends SqlServerBasePlatform {

  public SqlServer16Platform() {
    super();
    this.platform = Platform.SQLSERVER16;
    // default to use Identity rather than sequences
    this.dbIdentity.setIdType(IdType.IDENTITY);

    // non-utf8 column types
    DbPlatformType text = new DbPlatformType("text", false);
    DbPlatformType varchar = new DbPlatformType("varchar", 255, 8000, text);
    dbTypeMap.put(DbType.CHAR, new DbPlatformType("char", 1));
    dbTypeMap.put(DbType.VARCHAR, varchar);
    dbTypeMap.put(DbType.LONGVARCHAR, text);
    dbTypeMap.put(DbType.CLOB, text);
    dbTypeMap.put(DbType.JSON, text);
    dbTypeMap.put(DbType.JSONB, text);

    // old binary type
    dbTypeMap.put(DbType.BLOB, new DbPlatformType("image", false));
    dbTypeMap.put(DbType.VARBINARY, new DbPlatformType("image", false));
    dbTypeMap.put(DbType.LONGVARBINARY, new DbPlatformType("image", false));
  }

}
