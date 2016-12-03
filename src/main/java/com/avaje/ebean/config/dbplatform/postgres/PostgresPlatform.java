package com.avaje.ebean.config.dbplatform.postgres;

import com.avaje.ebean.BackgroundExecutor;
import com.avaje.ebean.config.ServerConfig;
import com.avaje.ebean.config.dbplatform.DatabasePlatform;
import com.avaje.ebean.config.dbplatform.DbPlatformType;
import com.avaje.ebean.config.dbplatform.DbType;
import com.avaje.ebean.config.dbplatform.IdType;
import com.avaje.ebean.config.dbplatform.PlatformIdGenerator;
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

    DbPlatformType dbTypeText = new DbPlatformType("text", false);
    DbPlatformType dbBytea = new DbPlatformType("bytea", false);

    dbTypeMap.put(DbType.UUID, new DbPlatformType("uuid", false));
    dbTypeMap.put(DbType.HSTORE, new DbPlatformType("hstore", false));
    dbTypeMap.put(DbType.JSON, new DbPlatformType("json", false));
    dbTypeMap.put(DbType.JSONB, new DbPlatformType("jsonb", false));

    dbTypeMap.put(DbType.INTEGER, new DbPlatformType("integer", false));
    dbTypeMap.put(DbType.DOUBLE, new DbPlatformType("float"));
    dbTypeMap.put(DbType.TINYINT, new DbPlatformType("smallint"));
    dbTypeMap.put(DbType.DECIMAL, new DbPlatformType("decimal", 38));
    dbTypeMap.put(DbType.TIMESTAMP, new DbPlatformType("timestamptz"));

    dbTypeMap.put(DbType.BINARY, dbBytea);
    dbTypeMap.put(DbType.VARBINARY, dbBytea);

    dbTypeMap.put(DbType.BLOB, dbBytea);
    dbTypeMap.put(DbType.CLOB, dbTypeText);
    dbTypeMap.put(DbType.LONGVARBINARY, dbBytea);
    dbTypeMap.put(DbType.LONGVARCHAR, dbTypeText);
  }

  protected void addGeoTypes(int srid) {
    dbTypeMap.put(DbType.POINT, geoType("point", srid));
    dbTypeMap.put(DbType.POLYGON, geoType("polygon", srid));
    dbTypeMap.put(DbType.LINESTRING, geoType("linestring", srid));
    dbTypeMap.put(DbType.MULTIPOINT, geoType("multipoint", srid));
    dbTypeMap.put(DbType.MULTILINESTRING, geoType("multilinestring", srid));
    dbTypeMap.put(DbType.MULTIPOLYGON, geoType("multipolygon", srid));
  }

  private DbPlatformType geoType(String type, int srid) {
    return new DbPlatformType("geometry(" + type + "," + srid + ")");
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
