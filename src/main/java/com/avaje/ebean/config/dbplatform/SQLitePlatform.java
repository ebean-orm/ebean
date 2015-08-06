package com.avaje.ebean.config.dbplatform;

import com.avaje.ebean.dbmigration.ddlgeneration.platform.SQLiteDdl;

import java.sql.Types;

public class SQLitePlatform extends DatabasePlatform {

  public SQLitePlatform() {
    super();
    this.name = "sqlite";
    this.platformDdl = new SQLiteDdl(dbTypeMap, dbIdentity);

    this.dbIdentity.setIdType(IdType.IDENTITY);
    this.dbIdentity.setSupportsGetGeneratedKeys(false);
    this.dbIdentity.setSupportsSequence(false);
    this.dbIdentity.setSelectLastInsertedIdTemplate("select last_insert_rowid()");

    this.booleanDbType = Types.INTEGER;

    dbTypeMap.put(Types.BIT, new DbType("int default 0"));
    dbTypeMap.put(Types.BOOLEAN, new DbType("int default 0"));
    dbTypeMap.put(Types.BIGINT, new DbType("integer"));
    dbTypeMap.put(Types.SMALLINT, new DbType("integer"));

  }

}
