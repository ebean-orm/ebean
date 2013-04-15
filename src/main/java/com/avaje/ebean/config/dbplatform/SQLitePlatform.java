package com.avaje.ebean.config.dbplatform;

import java.sql.Types;

import javax.sql.DataSource;

import com.avaje.ebean.BackgroundExecutor;

public class SQLitePlatform extends DatabasePlatform {

  public SQLitePlatform() {
    super();
    this.name = "sqlite";

    this.dbIdentity.setIdType(IdType.IDENTITY);
    this.dbIdentity.setSupportsGetGeneratedKeys(false);
    this.dbIdentity.setSelectLastInsertedIdTemplate("select last_insert_rowid()");
    this.openQuote = "\"";
    this.closeQuote = "\"";

    this.booleanDbType = Types.INTEGER;

    dbTypeMap.put(Types.BIT, new DbType("int default 0"));
    dbTypeMap.put(Types.BOOLEAN, new DbType("int default 0"));

    dbDdlSyntax.setInlinePrimaryKeyConstraint(true);
    dbDdlSyntax.setIdentity("AUTOINCREMENT");
    dbDdlSyntax.setDisableReferentialIntegrity("PRAGMA foreign_keys = OFF");
    dbDdlSyntax.setEnableReferentialIntegrity("PRAGMA foreign_keys = ON");
  }

  /**
   * Return null in case there is a sequence annotation.
   */
  @Override
  public IdGenerator createSequenceIdGenerator(BackgroundExecutor be,
      DataSource ds, String seqName, int batchSize) {

    return null;
  }

}
