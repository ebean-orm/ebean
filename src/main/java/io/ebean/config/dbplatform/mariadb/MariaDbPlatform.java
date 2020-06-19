package io.ebean.config.dbplatform.mariadb;

import io.ebean.annotation.Platform;
import io.ebean.config.dbplatform.mysql.BaseMySqlPlatform;

/**
 * MariaDB platform.
 */
public class MariaDbPlatform extends BaseMySqlPlatform {

  public MariaDbPlatform() {
    super();
    this.platform = Platform.MARIADB;
    this.historySupport = new MariaDbHistorySupport();
  }
}
