package io.ebean.config.dbplatform.mysql;

import io.ebean.BackgroundExecutor;
import io.ebean.annotation.Platform;
import io.ebean.Query;
import io.ebean.config.dbplatform.DatabasePlatform;
import io.ebean.config.dbplatform.DbPlatformType;
import io.ebean.config.dbplatform.DbType;
import io.ebean.config.dbplatform.IdType;
import io.ebean.config.dbplatform.PlatformIdGenerator;
import io.ebean.config.dbplatform.SqlErrorCodes;
import io.ebean.dbmigration.ddlgeneration.platform.MySqlDdl;

import javax.sql.DataSource;
import java.sql.Types;

/**
 * MySQL specific platform.
 * <p>
 * <ul>
 * <li>supportsGetGeneratedKeys = true</li>
 * <li>Uses LIMIT OFFSET clause</li>
 * <li>Uses ` for quoted identifiers</li>
 * </ul>
 * </p>
 */
public class MySqlPlatform extends DatabasePlatform {

  public MySqlPlatform() {
    super();
    this.platform = Platform.MYSQL;
    this.useExtraTransactionOnIterateSecondaryQueries = true;
    this.selectCountWithAlias = true;
    this.dbEncrypt = new MySqlDbEncrypt();
    this.platformDdl = new MySqlDdl(this);
    this.historySupport = new MySqlHistorySupport();
    this.columnAliasPrefix = null;

    this.dbIdentity.setIdType(IdType.IDENTITY);
    this.dbIdentity.setSupportsGetGeneratedKeys(true);
    this.dbIdentity.setSupportsIdentity(true);
    this.dbIdentity.setSupportsSequence(false);

    this.exceptionTranslator =
      new SqlErrorCodes()
        .addAcquireLock("1205")
        .addDuplicateKey("1062")
        .addDataIntegrity("630","839","840","893","1169","1215","1216","1217","1364","1451","1452","1557","23000")
        .build();

    this.openQuote = "`";
    this.closeQuote = "`";

    this.forwardOnlyHintOnFindIterate = true;
    this.booleanDbType = Types.BIT;

    dbTypeMap.put(DbType.BIT, new DbPlatformType("tinyint(1) default 0"));
    dbTypeMap.put(DbType.BOOLEAN, new DbPlatformType("tinyint(1) default 0"));
    dbTypeMap.put(DbType.TIMESTAMP, new DbPlatformType("datetime(6)"));
    dbTypeMap.put(DbType.CLOB, new MySqlClob());
    dbTypeMap.put(DbType.BLOB, new MySqlBlob());
    dbTypeMap.put(DbType.BINARY, new DbPlatformType("binary", 255));
    dbTypeMap.put(DbType.VARBINARY, new DbPlatformType("varbinary", 255));
  }

  /**
   * Return null in case there is a sequence annotation.
   */
  @Override
  public PlatformIdGenerator createSequenceIdGenerator(BackgroundExecutor be,
                                                       DataSource ds, String seqName, int batchSize) {

    return null;
  }

  @Override
  protected String withForUpdate(String sql, Query.ForUpdate forUpdateMode) {
    // NOWAIT and SKIP LOCKED currently not supported with MySQL
    return sql + " for update";
  }
}
