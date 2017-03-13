package io.ebean.config.dbplatform.db2;

import io.ebean.BackgroundExecutor;
import io.ebean.Platform;
import io.ebean.config.dbplatform.DatabasePlatform;
import io.ebean.config.dbplatform.DbPlatformType;
import io.ebean.config.dbplatform.DbType;
import io.ebean.config.dbplatform.PlatformIdGenerator;
import io.ebean.config.dbplatform.SqlErrorCodes;
import io.ebean.dbmigration.ddlgeneration.platform.DB2Ddl;

import javax.sql.DataSource;
import java.sql.Types;

/**
 * DB2 specific platform.
 */
public class DB2Platform extends DatabasePlatform {

  public DB2Platform() {
    super();
    this.platform = Platform.DB2;
    this.maxTableNameLength = 18;
    this.maxConstraintNameLength = 18;
    this.sqlLimiter = new Db2SqlLimiter();
    this.platformDdl = new DB2Ddl(this);

    this.dbIdentity.setSupportsGetGeneratedKeys(true);
    this.dbIdentity.setSupportsSequence(true);

    this.exceptionTranslator =
      new SqlErrorCodes()
        //.addAcquireLock("")
        .addDuplicateKey("-803")
        .addDataIntegrity("-407","-530","-531","-532","-543","-544","-545","-603","-667")
        .build();

    booleanDbType = Types.BOOLEAN;
    dbTypeMap.put(DbType.REAL, new DbPlatformType("real"));
    dbTypeMap.put(DbType.TINYINT, new DbPlatformType("smallint"));
    dbTypeMap.put(DbType.DECIMAL, new DbPlatformType("decimal", 15));
  }

  /**
   * Return a DB2 specific sequence IdGenerator that supports batch fetching
   * sequence values.
   */
  @Override
  public PlatformIdGenerator createSequenceIdGenerator(BackgroundExecutor be,
                                                       DataSource ds, String seqName, int batchSize) {

    return new DB2SequenceIdGenerator(be, ds, seqName, batchSize);
  }

}
