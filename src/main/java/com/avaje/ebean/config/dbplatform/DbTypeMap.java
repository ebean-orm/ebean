package com.avaje.ebean.config.dbplatform;

import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

/**
 * Used to map bean property types to DB specific types for DDL generation.
 */
public class DbTypeMap {

  private final Map<Integer, DbType> typeMap = new HashMap<Integer, DbType>();

  public DbTypeMap() {
    loadDefaults();
  }

  /**
   * Load the standard types. These can be overridden by DB specific platform.
   */
  private void loadDefaults() {

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
    put(Types.LONGVARBINARY, new DbType("longvarbinary"));
    put(Types.LONGVARCHAR, new DbType("lonvarchar"));
    put(Types.VARBINARY, new DbType("varbinary", 255));
    put(Types.BINARY, new DbType("binary", 255));

    put(Types.DATE, new DbType("date"));
    put(Types.TIME, new DbType("time"));
    put(Types.TIMESTAMP, new DbType("timestamp"));

  }

  /**
   * Override the type for a given JDBC type.
   */
  public void put(int jdbcType, DbType dbType) {
    typeMap.put(Integer.valueOf(jdbcType), dbType);
  }

  /**
   * Return the type for a given jdbc type.
   */
  public DbType get(int jdbcType) {

    DbType dbType = typeMap.get(Integer.valueOf(jdbcType));
    if (dbType == null) {
      String m = "No DB type for JDBC type " + jdbcType;
      throw new RuntimeException(m);
    }

    return dbType;
  }
}
