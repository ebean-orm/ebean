package com.avaje.ebean.config.dbplatform;

import com.avaje.ebean.BackgroundExecutor;
import com.avaje.ebean.config.ServerConfig;
import com.avaje.ebean.dbmigration.ddlgeneration.DdlHandler;
import com.avaje.ebean.dbmigration.ddlgeneration.platform.PostgresDdl;

import javax.sql.DataSource;
import java.sql.Types;

/**
 * Postgres v9 specific platform.
 * <p>
 * Uses serial types and getGeneratedKeys.
 * </p>
 */
public class PostgresPlatform extends DatabasePlatform {

  public PostgresPlatform() {
    super();
    this.name = "postgres";

    // OnQueryOnly.CLOSE as a performance optimisation on Postgres
    this.onQueryOnly = OnQueryOnly.CLOSE;
    this.likeClause = "like ? escape''";

    this.selectCountWithAlias = true;
    this.blobDbType = Types.LONGVARBINARY;
    this.clobDbType = Types.VARCHAR;

    this.dbEncrypt = new PostgresDbEncrypt();
    this.historySupport = new PostgresHistorySupport();
    this.platformDdl = new PostgresDdl(this.dbTypeMap, this.dbIdentity);

    // Use Identity and getGeneratedKeys
    this.dbIdentity.setIdType(IdType.IDENTITY);
    this.dbIdentity.setSupportsGetGeneratedKeys(true);
    this.dbIdentity.setSupportsSequence(true);

    //this.columnAliasPrefix = "as c";

    this.openQuote = "\"";
    this.closeQuote = "\"";

    DbType dbTypeText = new DbType("text");
    DbType dbBytea = new DbType("bytea", false);

    dbTypeMap.put(DbType.HSTORE, new DbType("hstore"));
    dbTypeMap.put(DbType.JSON, new DbType("json"));
    dbTypeMap.put(DbType.JSONB, new DbType("jsonb"));

    dbTypeMap.put(Types.INTEGER, new DbType("integer", false));
    dbTypeMap.put(Types.DOUBLE, new DbType("float"));
    dbTypeMap.put(Types.TINYINT, new DbType("smallint"));
    dbTypeMap.put(Types.DECIMAL, new DbType("decimal", 38));

    dbTypeMap.put(Types.BINARY, dbBytea);
    dbTypeMap.put(Types.VARBINARY, dbBytea);

    dbTypeMap.put(Types.BLOB, dbBytea);
    dbTypeMap.put(Types.CLOB, dbTypeText);
    dbTypeMap.put(Types.LONGVARBINARY, dbBytea);
    dbTypeMap.put(Types.LONGVARCHAR, dbTypeText);

  }

  /**
   * Return a DdlHandler instance for generating DDL for the specific platform.
   */
  public DdlHandler createDdlHandler(ServerConfig serverConfig) {
    return this.platformDdl.createDdlHandler(serverConfig);
  }

  /**
   * Create a Postgres specific sequence IdGenerator.
   */
  @Override
  public IdGenerator createSequenceIdGenerator(BackgroundExecutor be, DataSource ds, String seqName, int batchSize) {

    return new PostgresSequenceIdGenerator(be, ds, seqName, batchSize);
  }

  @Override
  protected String withForUpdate(String sql) {
    return sql + " for update";
  }
}
