package io.ebean.config.dbplatform.db2;

import io.ebean.annotation.Platform;

/**
 * DB2 platform for z/OS. 
 * Note: This platform is currently not tested!
 * @author Roland Praml, FOCONIS AG
 *
 */
public class DB2ZosPlatform extends BaseDB2Platform {
  public DB2ZosPlatform() {
    super();
    this.platform = Platform.DB2ZOS;
    this.maxTableNameLength = 128;
    this.maxConstraintNameLength = 128;
  }
}
