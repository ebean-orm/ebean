package io.ebean.config.dbplatform.mysql;

import io.ebean.annotation.Platform;

/**
 * MySQL specific platform.
 */
public class MySqlPlatform extends BaseMySqlPlatform {

  public MySqlPlatform() {
    super();
    this.platform = Platform.MYSQL;
  }

}
