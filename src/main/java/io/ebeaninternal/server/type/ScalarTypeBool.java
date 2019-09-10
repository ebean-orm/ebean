package io.ebeaninternal.server.type;

/**
 * Boolean ScalarType's must implement to support DDL default values etc.
 */
public interface ScalarTypeBool extends ScalarType<Boolean>, ScalarTypeLogicalType {

  /**
   * Return the DB literal value for FALSE.
   */
  String getDbFalseLiteral();

  /**
   * Return the DB literal value for TRUE.
   */
  String getDbTrueLiteral();
}
