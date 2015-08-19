package com.avaje.ebean.config.dbplatform;

import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

/**
 * Used to map bean property types to DB specific types for DDL generation.
 */
public class DbTypeMap {

  private static final DbType JSON_CLOB_PLACEHOLDER = new DbType("jsonClobPlaceholder");
  private static final DbType JSON_BLOB_PLACEHOLDER = new DbType("jsonBlobPlaceholder");
  private static final DbType JSON_VARCHAR_PLACEHOLDER = new DbType("jsonVarcharPlaceholder");

  /**
   * A map to reverse lookup the type by name.
   * <p>
   * Used when converting from logical types to platform types which we
   * want to do with 2 phase DDL generation.
   */
  static Map<String, Integer> lookup = new HashMap<String, Integer>();

  static {
    lookup.put("BOOLEAN", Types.BOOLEAN);
    lookup.put("BIT", Types.BIT);
    lookup.put("INTEGER", Types.INTEGER);
    lookup.put("BIGINT", Types.BIGINT);
    lookup.put("REAL", Types.REAL);
    // Float is most common REAL mapping to have that as well
    lookup.put("FLOAT", Types.REAL);

    lookup.put("DOUBLE", Types.DOUBLE);
    lookup.put("SMALLINT", Types.SMALLINT);
    lookup.put("TINYINT", Types.TINYINT);
    lookup.put("DECIMAL", Types.DECIMAL);
    lookup.put("VARCHAR", Types.VARCHAR);
    // VARCHAR2 - extra for Oracle specific column definition
    lookup.put("VARCHAR2", Types.VARCHAR);
    lookup.put("CHAR", Types.CHAR);
    lookup.put("BLOB", Types.BLOB);
    lookup.put("CLOB", Types.CLOB);

    lookup.put("LONGVARBINARY", Types.LONGVARBINARY);
    lookup.put("LONGVARCHAR", Types.LONGVARCHAR);
    lookup.put("VARBINARY", Types.VARBINARY);
    lookup.put("BINARY", Types.BINARY);
    lookup.put("DATE", Types.DATE);
    lookup.put("TIME", Types.TIME);
    lookup.put("TIMESTAMP", Types.TIMESTAMP);

    // Not standard java.sql.Types
    // logical JSON storage types
    lookup.put("JSON", DbType.JSON);
    lookup.put("JSONB", DbType.JSONB);
    lookup.put("JSONCLOB", DbType.JSONClob);
    lookup.put("JSONBLOB", DbType.JSONBlob);
    lookup.put("JSONVARCHAR", DbType.JSONVarchar);
  }


  private final Map<Integer, DbType> typeMap = new HashMap<Integer, DbType>();

  /**
   * Return the DbTypeMap with standard (not platform specific) types.
   *
   * This has some extended JSON types (JSON, JSONB, JSONVarchar, JSONClob, JSONBlob).
   * These types get translated to specific database platform types during DDL generation.
   */
  public static DbTypeMap logicalTypes() {
    return new DbTypeMap(true);
  }

  public DbTypeMap() {
    loadDefaults(false);
  }

  private DbTypeMap(boolean logicalTypes) {
    loadDefaults(logicalTypes);
  }

  /**
   * Load the standard types. These can be overridden by DB specific platform.
   */
  private void loadDefaults(boolean logicalTypes) {

    put(Types.BOOLEAN, new DbType("boolean"));
    put(Types.BIT, new DbType("bit"));

    put(Types.INTEGER, new DbType("integer"));
    put(Types.BIGINT, new DbType("bigint"));
    put(Types.REAL, new DbType("float"));
    put(Types.DOUBLE, new DbType("double"));
    put(Types.SMALLINT, new DbType("smallint"));
    put(Types.TINYINT, new DbType("tinyint"));
    put(Types.DECIMAL, new DbType("decimal", 38));

    put(Types.VARCHAR, new DbType("varchar", 255));
    put(Types.CHAR, new DbType("char", 1));

    put(Types.BLOB, new DbType("blob"));
    put(Types.CLOB, new DbType("clob"));

    if (logicalTypes) {
      // keep it logical for 2 layer DDL generation
      put(DbType.HSTORE, new DbType("hstore"));
      put(DbType.JSON, new DbType("json"));
      put(DbType.JSONB, new DbType("jsonb"));
      put(DbType.JSONClob, new DbType("jsonclob"));
      put(DbType.JSONBlob, new DbType("jsonblob"));
      put(DbType.JSONVarchar, new DbType("jsonvarchar", 1000));

    } else {
      put(DbType.JSON, JSON_CLOB_PLACEHOLDER); // Postgres maps this to JSON
      put(DbType.JSONB, JSON_CLOB_PLACEHOLDER); // Postgres maps this to JSONB
      put(DbType.JSONClob, JSON_CLOB_PLACEHOLDER);
      put(DbType.JSONBlob, JSON_BLOB_PLACEHOLDER);
      put(DbType.JSONVarchar, JSON_VARCHAR_PLACEHOLDER);
    }

    put(Types.LONGVARBINARY, new DbType("longvarbinary"));
    put(Types.LONGVARCHAR, new DbType("lonvarchar"));
    put(Types.VARBINARY, new DbType("varbinary", 255));
    put(Types.BINARY, new DbType("binary", 255));

    put(Types.DATE, new DbType("date"));
    put(Types.TIME, new DbType("time"));
    put(Types.TIMESTAMP, new DbType("timestamp"));

  }

  /**
   * Lookup the platform specific DbType given the standard sql type name.
   */
  public DbType lookup(String name) {
    name = name.trim().toUpperCase();
    Integer typeKey = lookup.get(name);
    if (typeKey == null) {
      throw new IllegalArgumentException("Unknown type [" + name + "] - not standard sql type");
    }
    // handle JSON types mapped to clob, blob and varchar
    switch (typeKey) {
      case DbType.JSONBlob:
        return get(Types.BLOB);
      case DbType.JSONClob:
        return get(Types.CLOB);
      case DbType.JSONVarchar:
        return get(Types.VARCHAR);
      case DbType.JSON:
        return getJsonType(DbType.JSON);
      case DbType.JSONB:
        return getJsonType(DbType.JSONB);
      default:
        return get(typeKey);
    }
  }

  private DbType getJsonType(int type) {
    DbType dbType = get(type);
    if (dbType == JSON_CLOB_PLACEHOLDER) {
      return get(Types.CLOB);
    }
    if (dbType == JSON_BLOB_PLACEHOLDER) {
      return get(Types.BLOB);
    }
    if (dbType == JSON_VARCHAR_PLACEHOLDER) {
      return get(Types.VARCHAR);
    }
    // Postgres has specific type
    return get(type);
  }

  /**
   * Override the type for a given JDBC type.
   */
  public void put(int jdbcType, DbType dbType) {
    typeMap.put(jdbcType, dbType);
  }

  /**
   * Return the type for a given jdbc type.
   */
  public DbType get(int jdbcType) {
    return typeMap.get(jdbcType);
  }
}
