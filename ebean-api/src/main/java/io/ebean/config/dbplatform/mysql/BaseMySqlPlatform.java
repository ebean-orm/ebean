package io.ebean.config.dbplatform.mysql;

import io.ebean.Query;
import io.ebean.config.dbplatform.DatabasePlatform;
import io.ebean.config.dbplatform.DbPlatformType;
import io.ebean.config.dbplatform.DbType;
import io.ebean.config.dbplatform.IdType;
import io.ebean.config.dbplatform.SqlErrorCodes;

import java.sql.Types;

/**
 * Base platform for both MySql and MariaDB.
 */
public abstract class BaseMySqlPlatform extends DatabasePlatform {

  public BaseMySqlPlatform() {
    super();
    this.useExtraTransactionOnIterateSecondaryQueries = true;
    this.selectCountWithAlias = true;
    this.supportsSavepointId = false;
    this.inlineSqlUpdateLimit = true;
    this.dbEncrypt = new MySqlDbEncrypt();
    this.historySupport = new MySqlHistorySupport();
    this.dbIdentity.setIdType(IdType.IDENTITY);
    this.dbIdentity.setSupportsGetGeneratedKeys(true);
    this.dbIdentity.setSupportsIdentity(true);
    this.dbIdentity.setSupportsSequence(false);

    this.dbDefaultValue.setNow("now(6)"); // must have same precision as TIMESTAMP
    this.dbDefaultValue.setFalse("0");
    this.dbDefaultValue.setTrue("1");


    this.exceptionTranslator =
      new SqlErrorCodes()
        .addAcquireLock("1205")
        .addDuplicateKey("1062", "1169")
        .addDataIntegrity("630", "839", "840", "893", "1215", "1216", "1217", "1364", "1451", "1452", "1557")
        .build();

    this.openQuote = "`";
    this.closeQuote = "`";
    // use pipe for escaping as it depends if mysql runs in no_backslash_escapes or not.
    this.likeClauseRaw = "like ? escape''";
    this.likeClauseEscaped = "like ? escape'|'";

    this.forwardOnlyHintOnFindIterate = true;
    this.booleanDbType = Types.BIT;

    dbTypeMap.put(DbType.BIT, new DbPlatformType("tinyint(1)"));
    dbTypeMap.put(DbType.BOOLEAN, new DbPlatformType("tinyint(1)"));
    dbTypeMap.put(DbType.TIMESTAMP, new DbPlatformType("datetime(6)"));
    dbTypeMap.put(DbType.CLOB, new MySqlClob());
    dbTypeMap.put(DbType.BLOB, new MySqlBlob());
    dbTypeMap.put(DbType.BINARY, new DbPlatformType("binary", 255));
    dbTypeMap.put(DbType.VARBINARY, new DbPlatformType("varbinary", 255));
    dbTypeMap.put(DbType.JSON, new DbPlatformType("json", false));
    dbTypeMap.put(DbType.JSONB, new DbPlatformType("json", false));
  }

  @Override
  protected String withForUpdate(String sql, Query.LockWait lockWait, Query.LockType lockType) {
    // NOWAIT and SKIP LOCKED currently not supported with MySQL
    return sql + " for update";
  }

}
