package com.avaje.ebean.config.dbplatform;

import com.avaje.ebean.BackgroundExecutor;
import com.avaje.ebean.dbmigration.ddlgeneration.platform.HsqldbDdl;

import javax.sql.DataSource;
import java.sql.Types;

/**
 * H2 specific platform.
 */
public class HsqldbPlatform extends DatabasePlatform {

  public HsqldbPlatform() {
    super();
    this.name = "hsqldb";
    this.dbEncrypt = new H2DbEncrypt();
    this.platformDdl = new HsqldbDdl(dbTypeMap, dbIdentity);

    this.dbIdentity.setIdType(IdType.IDENTITY);
    this.dbIdentity.setSupportsGetGeneratedKeys(true);
    this.dbIdentity.setSupportsSequence(true);
    this.dbIdentity.setSupportsIdentity(true);

    dbTypeMap.put(Types.INTEGER, new DbType("integer", false));
  }

  @Override
  public IdGenerator createSequenceIdGenerator(BackgroundExecutor be, DataSource ds, String seqName, int batchSize) {

    return new H2SequenceIdGenerator(be, ds, seqName, batchSize);
  }

}
