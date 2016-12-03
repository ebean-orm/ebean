package com.avaje.ebean.config.dbplatform.db2;

import com.avaje.ebean.BackgroundExecutor;
import com.avaje.ebean.config.dbplatform.DatabasePlatform;
import com.avaje.ebean.config.dbplatform.DbPlatformType;
import com.avaje.ebean.config.dbplatform.DbType;
import com.avaje.ebean.config.dbplatform.PlatformIdGenerator;
import com.avaje.ebean.dbmigration.ddlgeneration.platform.DB2Ddl;

import javax.sql.DataSource;
import java.sql.Types;

/**
 * DB2 specific platform.
 */
public class DB2Platform extends DatabasePlatform {

  public DB2Platform() {
    super();
    this.name = "db2";
    this.maxTableNameLength = 18;
    this.maxConstraintNameLength = 18;
    this.sqlLimiter = new Db2SqlLimiter();
    this.platformDdl = new DB2Ddl(this);

    this.dbIdentity.setSupportsGetGeneratedKeys(true);
    this.dbIdentity.setSupportsSequence(true);

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
