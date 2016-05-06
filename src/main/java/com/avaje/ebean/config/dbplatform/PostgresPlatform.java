package com.avaje.ebean.config.dbplatform;

import com.avaje.ebean.BackgroundExecutor;
import com.avaje.ebean.config.ServerConfig;
import com.avaje.ebean.dbmigration.ddlgeneration.DdlHandler;
import com.avaje.ebean.dbmigration.ddlgeneration.platform.PostgresDdl;

import javax.sql.DataSource;
import java.sql.Types;
import java.util.Properties;

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
    this.supportsNativeIlike = true;
    this.likeClause = "like ? escape''";
    this.selectCountWithAlias = true;
    this.blobDbType = Types.LONGVARBINARY;
    this.clobDbType = Types.VARCHAR;
    this.nativeUuidType = true;

    this.dbEncrypt = new PostgresDbEncrypt();
    this.historySupport = new PostgresHistorySupport();
    this.platformDdl = new PostgresDdl(this);

    // Use Identity and getGeneratedKeys
    this.dbIdentity.setIdType(IdType.IDENTITY);
    this.dbIdentity.setSupportsGetGeneratedKeys(true);
    this.dbIdentity.setSupportsSequence(true);

    //this.columnAliasPrefix = "as c";

    this.openQuote = "\"";
    this.closeQuote = "\"";

    DbType dbTypeText = new DbType("text");
    DbType dbBytea = new DbType("bytea", false);

    dbTypeMap.put(DbType.HSTORE, new DbType("hstore", false));
    dbTypeMap.put(DbType.JSON, new DbType("json", false));
    dbTypeMap.put(DbType.JSONB, new DbType("jsonb", false));

    dbTypeMap.put(Types.INTEGER, new DbType("integer", false));
    dbTypeMap.put(Types.DOUBLE, new DbType("float"));
    dbTypeMap.put(Types.TINYINT, new DbType("smallint"));
    dbTypeMap.put(Types.DECIMAL, new DbType("decimal", 38));
    dbTypeMap.put(Types.TIMESTAMP, new DbType("timestamptz"));

    dbTypeMap.put(Types.BINARY, dbBytea);
    dbTypeMap.put(Types.VARBINARY, dbBytea);

    dbTypeMap.put(Types.BLOB, dbBytea);
    dbTypeMap.put(Types.CLOB, dbTypeText);
    dbTypeMap.put(Types.LONGVARBINARY, dbBytea);
    dbTypeMap.put(Types.LONGVARCHAR, dbTypeText);
  }

  @Override
  public void configure(Properties properties) {
    super.configure(properties);
    String tsType = properties.getProperty("ebean.postgres.timestamp");
    if (tsType != null) {
      // set timestamp type to "timestamp" without time zone
      dbTypeMap.put(Types.TIMESTAMP, new DbType(tsType));
    }
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
  public PlatformIdGenerator createSequenceIdGenerator(BackgroundExecutor be, DataSource ds, String seqName, int batchSize) {

    return new PostgresSequenceIdGenerator(be, ds, seqName, batchSize);
  }

  @Override
  protected String withForUpdate(String sql) {
    return sql + " for update";
  }
}
