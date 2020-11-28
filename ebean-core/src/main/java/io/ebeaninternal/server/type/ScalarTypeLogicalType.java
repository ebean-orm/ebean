package io.ebeaninternal.server.type;

/**
 * Marks types that can be mapped differently to different DB platforms.
 */
public interface ScalarTypeLogicalType {

  /**
   * Return the DB agnostic logical type.
   */
  int getLogicalType();
}
