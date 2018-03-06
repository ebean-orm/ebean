package io.ebean.config.dbplatform.hsqldb;

import io.ebean.BackgroundExecutor;
import io.ebean.annotation.Platform;
import io.ebean.config.dbplatform.DatabasePlatform;
import io.ebean.config.dbplatform.DbPlatformType;
import io.ebean.config.dbplatform.DbType;
import io.ebean.config.dbplatform.IdType;
import io.ebean.config.dbplatform.PlatformIdGenerator;
import io.ebean.config.dbplatform.h2.H2DbEncrypt;
import io.ebean.config.dbplatform.h2.H2SequenceIdGenerator;

import javax.sql.DataSource;

/**
 * H2 specific platform.
 */
public class HsqldbPlatform extends DatabasePlatform {

  public HsqldbPlatform() {
    super();
    this.platform = Platform.HSQLDB;
    this.dbEncrypt = new H2DbEncrypt();

    this.dbIdentity.setIdType(IdType.IDENTITY);
    this.dbIdentity.setSupportsGetGeneratedKeys(true);
    this.dbIdentity.setSupportsSequence(true);
    this.dbIdentity.setSupportsIdentity(true);

    dbTypeMap.put(DbType.INTEGER, new DbPlatformType("integer", false));
  }

  @Override
  public PlatformIdGenerator createSequenceIdGenerator(BackgroundExecutor be, DataSource ds, int stepSize, String seqName) {
    return new H2SequenceIdGenerator(be, ds, seqName, sequenceBatchSize);
  }

}
