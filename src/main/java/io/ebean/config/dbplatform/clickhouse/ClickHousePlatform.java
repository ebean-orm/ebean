package io.ebean.config.dbplatform.clickhouse;

import io.ebean.annotation.Platform;
import io.ebean.config.dbplatform.DatabasePlatform;
import io.ebean.config.dbplatform.DbPlatformType;
import io.ebean.config.dbplatform.DbType;
import io.ebean.config.dbplatform.IdType;

import java.sql.Types;

public class ClickHousePlatform extends DatabasePlatform {

  public ClickHousePlatform() {
    super();
    this.platform = Platform.CLICKHOUSE;
    //this.dbEncrypt =
    //this.historySupport =
    //this.exceptionTranslator =

    this.nativeUuidType = true;
    this.dbDefaultValue.setNow("now()");
    this.columnAliasPrefix = null;

    this.dbIdentity.setIdType(IdType.IDENTITY);
    this.dbIdentity.setSupportsGetGeneratedKeys(false);
    this.dbIdentity.setSupportsSequence(false);
    this.dbIdentity.setSupportsIdentity(true);

    this.booleanDbType = Types.INTEGER;
    dbTypeMap.put(DbType.BOOLEAN, new DbPlatformType("UInt8"));

    // using unsigned as default types ...
    dbTypeMap.put(DbType.TINYINT, new DbPlatformType("UInt8", false));
    dbTypeMap.put(DbType.SMALLINT, new DbPlatformType("UInt16", false));
    dbTypeMap.put(DbType.INTEGER, new DbPlatformType("UInt32", false));
    dbTypeMap.put(DbType.BIGINT, new DbPlatformType("UInt64", false));
    dbTypeMap.put(DbType.DECIMAL, new DbPlatformType("Decimal", 16,4));
    dbTypeMap.put(DbType.DOUBLE, new DbPlatformType("Float64", false));

    dbTypeMap.put(DbType.DATE, new DbPlatformType("Date", false));
    dbTypeMap.put(DbType.TIMESTAMP, new DbPlatformType("DateTime", false));

    dbTypeMap.put(DbType.VARCHAR, new DbPlatformType("String", false));
    dbTypeMap.put(DbType.LONGVARCHAR, new DbPlatformType("String", false));
    dbTypeMap.put(DbType.CLOB, new DbPlatformType("String", false));
    dbTypeMap.put(DbType.JSONVARCHAR, new DbPlatformType("String", false));

    dbTypeMap.put(DbType.UUID, new DbPlatformType("UUID", false));
    dbTypeMap.put(DbType.INET, new DbPlatformType("String", false));
    dbTypeMap.put(DbType.CDIR, new DbPlatformType("String", false));
  }

  @Override
  public boolean isNativeArrayType() {
    return true;
  }

}
