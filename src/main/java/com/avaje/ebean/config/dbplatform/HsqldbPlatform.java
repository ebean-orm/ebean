package com.avaje.ebean.config.dbplatform;

import com.avaje.ebean.BackgroundExecutor;
import com.avaje.ebean.dbmigration.ddlgeneration.platform.HsqldbDdl;

import javax.sql.DataSource;

/**
 * H2 specific platform.
 */
public class HsqldbPlatform extends DatabasePlatform {

  public HsqldbPlatform() {
    super();
    this.name = "hsqldb";
    this.dbEncrypt = new H2DbEncrypt();
    this.platformDdl = new HsqldbDdl(this);

    this.dbIdentity.setIdType(IdType.IDENTITY);
    this.dbIdentity.setSupportsGetGeneratedKeys(true);
    this.dbIdentity.setSupportsSequence(true);
    this.dbIdentity.setSupportsIdentity(true);

    dbTypeMap.put(DbType.INTEGER, new DbPlatformType("integer", false));
  }

  @Override
  public PlatformIdGenerator createSequenceIdGenerator(BackgroundExecutor be, DataSource ds, String seqName, int batchSize) {

    return new H2SequenceIdGenerator(be, ds, seqName, batchSize);
  }

}
