package io.ebean.config.dbplatform.nuodb;

import io.ebean.BackgroundExecutor;
import io.ebean.Query;
import io.ebean.annotation.Platform;
import io.ebean.config.dbplatform.DatabasePlatform;
import io.ebean.config.dbplatform.DbPlatformType;
import io.ebean.config.dbplatform.DbType;
import io.ebean.config.dbplatform.IdType;
import io.ebean.config.dbplatform.PlatformIdGenerator;
import io.ebean.config.dbplatform.SqlErrorCodes;

import javax.sql.DataSource;

/**
 * NuoDb specific platform.
 */
public class NuoDbPlatform extends DatabasePlatform {

  public NuoDbPlatform() {
    super();
    this.platform = Platform.NUODB;
    this.idInExpandedForm = true;
    this.supportsResultSetConcurrencyModeUpdatable = false;
    this.supportsDeleteTableAlias = true;
    this.likeClauseRaw = "like ?";
    this.historySupport = new NuoDbHistorySupport();
    //this.dbEncrypt = ...
    dbIdentity.setIdType(IdType.IDENTITY);
    dbIdentity.setSupportsIdentity(true);
    dbIdentity.setSupportsSequence(true);
    dbIdentity.setSupportsGetGeneratedKeys(true);

    // No referential integrity exception to map
    this.exceptionTranslator =
      new SqlErrorCodes()
        //.addAcquireLock("")
        .addDuplicateKey("23000")
        .addSerializableConflict("40002")
        .build();

    dbTypeMap.put(DbType.INTEGER, new DbPlatformType("integer", false));
  }

  @Override
  public PlatformIdGenerator createSequenceIdGenerator(BackgroundExecutor be, DataSource ds, int stepSize, String seqName) {
    return new NuoDbSequence(be, ds, seqName, stepSize);
  }

  @Override
  protected String withForUpdate(String sql, Query.LockWait lockWait, Query.LockType lockType) {
    switch (lockWait) {
      case NOWAIT:
        return sql + " for update nowait";
      case SKIPLOCKED:
      default:
        return sql + " for update";
    }
  }

}
