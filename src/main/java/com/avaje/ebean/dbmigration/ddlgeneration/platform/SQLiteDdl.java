package com.avaje.ebean.dbmigration.ddlgeneration.platform;

import com.avaje.ebean.config.dbplatform.DbIdentity;
import com.avaje.ebean.config.dbplatform.DbTypeMap;

/**
 * DB2 platform specific DDL.
 */
public class SQLiteDdl extends PlatformDdl {

  public SQLiteDdl(DbTypeMap platformTypes, DbIdentity dbIdentity) {
    super(platformTypes, dbIdentity);
    this.identitySuffix = " autoincrement";
  }
  
}
