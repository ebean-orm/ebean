package io.ebean.config.dbplatform;

/**
 * Integer codes for the extra types beyond java.sql.Types.
 */
public interface ExtraDbTypes {

  /**
   * DB native UUID type (H2 and Postgres).
   */
  int UUID = 5010;

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

}
