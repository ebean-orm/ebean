package io.ebean.config.dbplatform.db2;

import io.ebean.annotation.Platform;

/**
 * DB2 platform for Linux/Unix/Windows.
 * @author Roland Praml, FOCONIS AG
 *
 */
public class DB2LuwPlatform extends BaseDB2Platform {
  public DB2LuwPlatform() {
    super();
    this.platform = Platform.DB2; // TODO DB2LUW
    this.maxTableNameLength = 128;
    this.maxConstraintNameLength = 128;
  }
}
