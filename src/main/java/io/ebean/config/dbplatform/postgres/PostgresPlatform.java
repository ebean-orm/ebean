package io.ebean.config.dbplatform.postgres;

import io.ebean.BackgroundExecutor;
import io.ebean.Query;
import io.ebean.annotation.PartitionMode;
import io.ebean.annotation.Platform;
import io.ebean.config.dbplatform.DatabasePlatform;
import io.ebean.config.dbplatform.DbPlatformType;
import io.ebean.config.dbplatform.DbType;
import io.ebean.config.dbplatform.IdType;
import io.ebean.config.dbplatform.PlatformIdGenerator;
import io.ebean.config.dbplatform.SqlErrorCodes;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
    this.platform = Platform.POSTGRES;
    this.supportsNativeIlike = true;
    this.selectCountWithAlias = true;
    this.blobDbType = Types.LONGVARBINARY;
    this.clobDbType = Types.VARCHAR;
    this.nativeUuidType = true;
    this.columnAliasPrefix = null;

    this.dbEncrypt = new PostgresDbEncrypt();
    this.historySupport = new PostgresHistorySupport();

    // Use Identity and getGeneratedKeys
    this.dbIdentity.setIdType(IdType.IDENTITY);
    this.dbIdentity.setSupportsGetGeneratedKeys(true);
    this.dbIdentity.setSupportsSequence(true);

    this.dbDefaultValue.setNow("current_timestamp");

    this.exceptionTranslator =
      new SqlErrorCodes()
        .addAcquireLock("55P03")
        .addDuplicateKey("23505")
        .addDataIntegrity("23000","23502","23503","23514")
        .build();

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

  @Override
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
   * So we can generate varchar[], int[], uuid[] column definitions and use the associated scalar types.
   */
  @Override
  public boolean isNativeArrayType() {
    return true;
  }

  /**
   * Create a Postgres specific sequence IdGenerator.
   */
  @Override
  public PlatformIdGenerator createSequenceIdGenerator(BackgroundExecutor be, DataSource ds, int stepSize, String seqName) {

    return new PostgresSequenceIdGenerator(be, ds, seqName, sequenceBatchSize);
  }

  @Override
  protected String withForUpdate(String sql, Query.ForUpdate forUpdateMode) {
    switch (forUpdateMode) {
      case SKIPLOCKED:
        return sql + " for update skip locked";
      case NOWAIT:
        return sql + " for update nowait";
      default:
        return sql + " for update";
    }
  }

  @Override
  public boolean tablePartitionsExist(Connection connection, String table) throws SQLException {

    try (PreparedStatement statement = connection.prepareStatement("select count(*) from pg_inherits i WHERE  i.inhparent = ?::regclass")) {
      statement.setString(1, table);
      try (ResultSet resultSet = statement.executeQuery()) {
        return resultSet.next() && resultSet.getInt(1) > 0;
      }
    }
  }

  /**
   * Return SQL using built in partition helper functions to create some initial partitions.
   *
   * Only use this if extra-dll doesn't have some initial partitions defined (which it should).
   */
  public String tablePartitionInit(String tableName, PartitionMode mode, String property, String pkey) {
    if (property == null) {
      property = "";
    }
    if (pkey == null) {
      pkey = "";
    }
    return "select partition('" + mode.name().toLowerCase() + "','" + tableName + "','" + pkey + "','" + property + "',1);";
  }

}
