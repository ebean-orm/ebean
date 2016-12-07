package com.avaje.ebean.config.dbplatform.hsqldb;

import com.avaje.ebean.BackgroundExecutor;
import com.avaje.ebean.config.Platform;
import com.avaje.ebean.config.dbplatform.DatabasePlatform;
import com.avaje.ebean.config.dbplatform.DbPlatformType;
import com.avaje.ebean.config.dbplatform.DbType;
import com.avaje.ebean.config.dbplatform.IdType;
import com.avaje.ebean.config.dbplatform.PlatformIdGenerator;
import com.avaje.ebean.config.dbplatform.h2.H2DbEncrypt;
import com.avaje.ebean.config.dbplatform.h2.H2SequenceIdGenerator;
import com.avaje.ebean.dbmigration.ddlgeneration.platform.HsqldbDdl;

import javax.sql.DataSource;

/**
 * H2 specific platform.
 */
public class HsqldbPlatform extends DatabasePlatform {

  public HsqldbPlatform() {
    super();
    this.platform = Platform.HSQLDB;
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
