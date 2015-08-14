package com.avaje.ebean.config.dbplatform;

import java.sql.Types;

import javax.sql.DataSource;

import com.avaje.ebean.BackgroundExecutor;
import com.avaje.ebean.dbmigration.ddlgeneration.platform.DB2Ddl;

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
    this.platformDdl = new DB2Ddl(dbTypeMap, dbIdentity);

    this.dbIdentity.setSupportsGetGeneratedKeys(true);
    this.dbIdentity.setSupportsSequence(true);

    booleanDbType = Types.BOOLEAN;
    dbTypeMap.put(Types.REAL, new DbType("real"));
    dbTypeMap.put(Types.TINYINT, new DbType("smallint"));
    dbTypeMap.put(Types.DECIMAL, new DbType("decimal", 15));
  }

  /**
   * Return a DB2 specific sequence IdGenerator that supports batch fetching
   * sequence values.
   */
  @Override
  public IdGenerator createSequenceIdGenerator(BackgroundExecutor be,
      DataSource ds, String seqName, int batchSize) {

    return new DB2SequenceIdGenerator(be, ds, seqName, batchSize);
  }

}
