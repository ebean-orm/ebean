package io.ebean.platform.db2;

import io.ebean.annotation.Platform;

/**
 * DB2 platform for Linux/Unix/Windows.
 * @author Roland Praml, FOCONIS AG
 *
 */
public class DB2LuwPlatform extends BaseDB2Platform {
  public DB2LuwPlatform() {
    super();
    this.platform = Platform.DB2LUW;
    this.maxTableNameLength = 128;
    this.maxConstraintNameLength = 128;
  }
}
