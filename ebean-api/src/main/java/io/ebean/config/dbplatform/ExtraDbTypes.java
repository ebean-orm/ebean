package io.ebean.config.dbplatform;

/**
 * Integer codes for the extra types beyond java.sql.Types.
 */
public interface ExtraDbTypes {

  /**
   * Type to map Map content to Postgres HSTORE.
   */
  int HSTORE = 5000;

  /**
   * Type to map JSON content to Clob or Postgres JSON type.
   */
  int JSON = 5001;

  /**
   * Type to map JSON content to Clob or Postgres JSONB type.
   */
  int JSONB = 5002;

  /**
   * Type to map JSON content to VARCHAR.
   */
  int JSONVarchar = 5003;

  /**
   * Type to map JSON content to Clob.
   */
  int JSONClob = 5004;

  /**
   * Type to map JSON content to Blob.
   */
  int JSONBlob = 5005;

  int LOCALDATETIME = 5009;

  /**
   * DB native UUID type (H2 and Postgres).
   */
  int UUID = 5010;
  int INET = 5020;
  int CIDR = 5021;

  /**
   * Geo Point
   */
  int POINT = 6000;

  /**
   * Geo Polygon
   */
  int POLYGON = 6001;

  /**
   * Geo Point
   */
  int LINESTRING = 6002;

  /**
   * Geo MultiPolygon
   */
  int MULTIPOINT = 6005;

  /**
   * Geo MultiPolygon
   */
  int MULTIPOLYGON = 6006;

  /**
   * Geo MultiPolygon
   */
  int MULTILINESTRING = 6007;


  /**
   * PGVector base type
   */
  int	VECTOR=10000;

  /**
   * PGVector half precision float type
   */
  int	VECTOR_HALF=10001;

  /**
   * PGVector binary type (bit)
   */
  int	VECTOR_BIT=10002;

  /**
   * PGVector sparse type
   */
  int VECTOR_SPARSE=10003;

}
