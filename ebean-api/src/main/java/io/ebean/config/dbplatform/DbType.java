package io.ebean.config.dbplatform;

import java.sql.Types;

/**
 * The known DB types that are mapped.
 * <p>
 * This includes extra types such as UUID, JSON, JSONB and HSTORE.
 * </p>
 */
public enum DbType {

  BOOLEAN(Types.BOOLEAN),
  BIT(Types.BIT),
  INTEGER(Types.INTEGER),
  BIGINT(Types.BIGINT),
  SMALLINT(Types.SMALLINT),
  TINYINT(Types.TINYINT),
  REAL(Types.REAL),
  //FLOAT(Types.FLOAT),
  DOUBLE(Types.DOUBLE),
  DECIMAL(Types.DECIMAL),
  VARCHAR(Types.VARCHAR),
  CHAR(Types.CHAR),
  BLOB(Types.BLOB),
  CLOB(Types.CLOB),
  LONGVARBINARY(Types.LONGVARBINARY),
  LONGVARCHAR(Types.LONGVARCHAR),
  VARBINARY(Types.VARBINARY),
  BINARY(Types.BINARY),
  DATE(Types.DATE),
  TIME(Types.TIME),
  TIMESTAMP(Types.TIMESTAMP),

  ARRAY(Types.ARRAY),

  LOCALDATETIME(ExtraDbTypes.LOCALDATETIME),
  UUID(ExtraDbTypes.UUID),
  INET(ExtraDbTypes.INET),
  CIDR(ExtraDbTypes.CIDR),

  POINT(ExtraDbTypes.POINT),
  POLYGON(ExtraDbTypes.POLYGON),
  LINESTRING(ExtraDbTypes.LINESTRING),
  MULTIPOINT(ExtraDbTypes.MULTIPOINT),
  MULTILINESTRING(ExtraDbTypes.MULTILINESTRING),
  MULTIPOLYGON(ExtraDbTypes.MULTIPOLYGON),

  HSTORE(ExtraDbTypes.HSTORE),
  JSON(ExtraDbTypes.JSON),
  JSONB(ExtraDbTypes.JSONB),
  JSONCLOB(ExtraDbTypes.JSONClob),
  JSONBLOB(ExtraDbTypes.JSONBlob),
  JSONVARCHAR(ExtraDbTypes.JSONVarchar),

  VECTOR(ExtraDbTypes.VECTOR),
  VECTOR_HALF(ExtraDbTypes.VECTOR_HALF),
  VECTOR_BIT(ExtraDbTypes.VECTOR_BIT),
  VECTOR_SPARSE(ExtraDbTypes.VECTOR_SPARSE);

  private final int id;

  DbType(int id) {
    this.id = id;
  }

  /**
   * Return the JDBC java.sql.Types value.
   */
  public int id() {
    return id;
  }

  /**
   * Create a platform type without scale or precision.
   */
  public DbPlatformType createPlatformType() {
    return new DbPlatformType(name().toLowerCase());
  }
}
