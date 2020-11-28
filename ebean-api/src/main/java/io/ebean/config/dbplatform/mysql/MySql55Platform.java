package io.ebean.config.dbplatform.mysql;

import io.ebean.annotation.Platform;
import io.ebean.config.dbplatform.DbPlatformType;
import io.ebean.config.dbplatform.DbType;

public class MySql55Platform extends BaseMySqlPlatform {

  public MySql55Platform() {
    super();
    this.platform = Platform.MYSQL55;
    dbTypeMap.put(DbType.TIMESTAMP, new DbPlatformType("datetime"));
  }
}
