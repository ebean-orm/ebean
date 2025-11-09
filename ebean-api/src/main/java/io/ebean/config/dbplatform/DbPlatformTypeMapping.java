package io.ebean.config.dbplatform;

import io.ebean.config.PlatformConfig;

import java.util.EnumMap;
import java.util.Map;

/**
 * Used to map bean property types to DB specific types for DDL generation.
 */
public class DbPlatformTypeMapping {

  /**
   * Boolean type used for logical model.
   */
  private static class BooleanLogicalType extends DbPlatformType {
    BooleanLogicalType() {
      super("boolean", false);
    }
    @Override
    protected void renderLengthScale(int deployLength, int deployScale, StringBuilder sb) {
      // do not have length - even if platform boolean type does like integer(1)
    }
  }

  private static final DbPlatformTypeLookup lookup = new DbPlatformTypeLookup();

  private static final DbPlatformType BOOLEAN_LOGICAL = new BooleanLogicalType();

  private static final DbPlatformType INET_NATIVE = new DbPlatformType("inet", false);
  private static final DbPlatformType INET_VARCHAR = new DbPlatformType("varchar", 50);
  private static final DbPlatformType CIDR_NATIVE = new DbPlatformType("cidr", false);
  private static final DbPlatformType CIDR_VARCHAR = new DbPlatformType("varchar", 50);

  private static final DbPlatformType UUID_NATIVE = new DbPlatformType("uuid", false);
  @SuppressWarnings("unused")
  private static final DbPlatformType UUID_PLACEHOLDER = new DbPlatformType("uuidPlaceholder");
  private static final DbPlatformType JSON_CLOB_PLACEHOLDER = new DbPlatformType("jsonClobPlaceholder");
  private static final DbPlatformType JSON_BLOB_PLACEHOLDER = new DbPlatformType("jsonBlobPlaceholder");
  private static final DbPlatformType JSON_VARCHAR_PLACEHOLDER = new DbPlatformType("jsonVarcharPlaceholder");

  private static final DbPlatformType POINT = new DbPlatformType("point");
  private static final DbPlatformType POLYGON = new DbPlatformType("polygon");
  private static final DbPlatformType LINESTRING = new DbPlatformType("linestring");
  private static final DbPlatformType MULTIPOINT = new DbPlatformType("multipoint");
  private static final DbPlatformType MULTILINESTRING = new DbPlatformType("multilinestring");
  private static final DbPlatformType MULTIPOLYGON = new DbPlatformType("multipolygon");

  private static final DbPlatformType VECTOR = new DbPlatformType("vector", 2000, null);
  private static final DbPlatformType VECTOR_HALF = new DbPlatformType("halfvec", 4000, null);
  private static final DbPlatformType VECTOR_BIT = new DbPlatformType("bit", 64000, null);
  private static final DbPlatformType VECTOR_SPARSE = new DbPlatformType("sparsevec", 1000, null);

  private final Map<DbType, DbPlatformType> typeMap = new EnumMap<>(DbType.class);

  /**
   * Return the DbTypeMap with standard (not platform specific) types.
   * <p>
   * This has some extended JSON types (JSON, JSONB, JSONVarchar, JSONClob, JSONBlob).
   * These types get translated to specific database platform types during DDL generation.
   */
  public static DbPlatformTypeMapping logicalTypes() {
    return new DbPlatformTypeMapping(true);
  }

  public DbPlatformTypeMapping() {
    loadDefaults(false);
  }

  private DbPlatformTypeMapping(boolean logicalTypes) {
    loadDefaults(logicalTypes);
  }

  /**
   * Load the standard types. These can be overridden by DB specific platform.
   */
  private void loadDefaults(boolean logicalTypes) {
    put(DbType.BOOLEAN, BOOLEAN_LOGICAL);
    put(DbType.BIT);
    put(DbType.INTEGER);
    put(DbType.BIGINT);
    put(DbType.DOUBLE);
    put(DbType.SMALLINT);
    put(DbType.TINYINT);
    put(DbType.BLOB);
    put(DbType.CLOB);
    put(DbType.ARRAY);
    put(DbType.DATE);
    put(DbType.TIME);
    put(DbType.TIMESTAMP);
    put(DbType.LONGVARBINARY);
    put(DbType.LONGVARCHAR);
    // most commonly real maps to db float
    put(DbType.REAL, new DbPlatformType("float"));
    put(DbType.POINT, POINT);
    put(DbType.POLYGON, POLYGON);
    put(DbType.LINESTRING, LINESTRING);
    put(DbType.MULTIPOINT, MULTIPOINT);
    put(DbType.MULTILINESTRING, MULTILINESTRING);
    put(DbType.MULTIPOLYGON, MULTIPOLYGON);
    put(DbType.VECTOR, VECTOR);
    put(DbType.VECTOR_HALF, VECTOR_HALF);
    put(DbType.VECTOR_BIT, VECTOR_BIT);
    put(DbType.VECTOR_SPARSE, VECTOR_SPARSE);

    if (logicalTypes) {
      // keep it logical for 2 layer DDL generation
      put(DbType.VARCHAR, new DbPlatformType("varchar"));
      put(DbType.DECIMAL, new DbPlatformType("decimal"));
      put(DbType.VARBINARY, new DbPlatformType("varbinary"));
      put(DbType.BINARY, new DbPlatformType("binary"));
      put(DbType.CHAR, new DbPlatformType("char"));
      put(DbType.LOCALDATETIME, new DbPlatformType("localdatetime", false));
      put(DbType.HSTORE, new DbPlatformType("hstore", false));
      put(DbType.JSON, new DbPlatformType("json", false));
      put(DbType.JSONB, new DbPlatformType("jsonb", false));
      put(DbType.JSONCLOB, new DbPlatformType("jsonclob"));
      put(DbType.JSONBLOB, new DbPlatformType("jsonblob"));
      put(DbType.JSONVARCHAR, new DbPlatformType("jsonvarchar", 1000));
      put(DbType.UUID, UUID_NATIVE);
      put(DbType.INET, INET_NATIVE);
      put(DbType.CIDR, CIDR_NATIVE);

    } else {
      put(DbType.VARCHAR, new DbPlatformType("varchar", 255));
      put(DbType.DECIMAL, new DbPlatformType("decimal", 16, 3));
      put(DbType.VARBINARY, new DbPlatformType("varbinary", 255));
      put(DbType.BINARY, new DbPlatformType("binary", 255));
      put(DbType.CHAR, new DbPlatformType("char", 1));
      put(DbType.LOCALDATETIME, DbType.TIMESTAMP.createPlatformType());
      put(DbType.JSON, JSON_CLOB_PLACEHOLDER); // Postgres maps this to JSON
      put(DbType.JSONB, JSON_CLOB_PLACEHOLDER); // Postgres maps this to JSONB
      put(DbType.JSONCLOB, JSON_CLOB_PLACEHOLDER);
      put(DbType.JSONBLOB, JSON_BLOB_PLACEHOLDER);
      put(DbType.JSONVARCHAR, JSON_VARCHAR_PLACEHOLDER);
      // default to native UUID and override on platform configure()
      put(DbType.UUID, UUID_NATIVE);
      put(DbType.INET, INET_VARCHAR);
      put(DbType.CIDR, CIDR_VARCHAR);
    }
  }

  /**
   * Lookup the platform specific DbType given the standard sql type name.
   */
  public DbPlatformType lookup(String name, boolean withScale) {
    DbType type = lookup.byName(name);
    if (type == null) {
      throw new IllegalArgumentException("Unknown type [" + name + "] - not standard sql type");
    }
    // handle JSON types mapped to clob, blob and varchar
    switch (type) {
      case JSONBLOB:
        return get(DbType.BLOB);
      case JSONCLOB:
        return get(DbType.CLOB);
      case JSONVARCHAR:
        return get(DbType.VARCHAR);
      case JSON:
        return getJsonType(DbType.JSON, withScale);
      case JSONB:
        return getJsonType(DbType.JSONB, withScale);
      default:
        return get(type);
    }
  }

  private DbPlatformType getJsonType(DbType type, boolean withScale) {
    DbPlatformType dbType = get(type);
    if (dbType == JSON_CLOB_PLACEHOLDER) {
      // if we have scale that implies this maps to varchar
      return withScale ? get(DbType.VARCHAR) : get(DbType.CLOB);
    }
    if (dbType == JSON_BLOB_PLACEHOLDER) {
      return get(DbType.BLOB);
    }
    if (dbType == JSON_VARCHAR_PLACEHOLDER) {
      return get(DbType.VARCHAR);
    }
    // Postgres has specific type
    return get(type);
  }

  /**
   * Override the type for a given JDBC type.
   */
  private void put(DbType type) {
    typeMap.put(type, type.createPlatformType());
  }

  /**
   * Override the type for a given JDBC type.
   */
  public void put(DbType type, DbPlatformType platformType) {
    typeMap.put(type, platformType);
  }

  /**
   * Return the type for a given jdbc type.
   */
  public DbPlatformType get(int jdbcType) {
    return get(lookup.byId(jdbcType));
  }

  /**
   * Return the type for a given jdbc type.
   */
  public DbPlatformType get(DbType dbType) {
    return typeMap.get(dbType);
  }

  /**
   * Map the UUID appropriately based on native DB support and DatabaseConfig.DbUuid.
   */
  public void config(boolean nativeUuidType, PlatformConfig.DbUuid dbUuid) {
    if (nativeUuidType && dbUuid.useNativeType()) {
      // native UUID already set by default
    } else if (dbUuid.useBinary()) {
      put(DbType.UUID, get(DbType.BINARY).withLength(16));
    } else {
      put(DbType.UUID, get(DbType.VARCHAR).withLength(40));
    }
  }
}
