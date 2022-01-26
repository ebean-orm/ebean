package io.ebean.config.dbplatform.mariadb;

import javax.sql.DataSource;

import io.ebean.BackgroundExecutor;
import io.ebean.annotation.Platform;
import io.ebean.config.dbplatform.PlatformIdGenerator;
import io.ebean.config.dbplatform.mysql.BaseMySqlPlatform;

/**
 * MariaDB platform.
 */
public class MariaDbPlatform extends BaseMySqlPlatform {

  public MariaDbPlatform() {
    super();
    this.platform = Platform.MARIADB;
    this.sequenceBatchMode = false;
    this.historySupport = new MariaDbHistorySupport();
    this.dbIdentity.setSupportsSequence(true);
  }

  @Override
  public PlatformIdGenerator createSequenceIdGenerator(BackgroundExecutor be, DataSource ds, int stepSize, String seqName) {
    return new MariaDbSequence(be, ds, seqName, stepSize);
  }

}
