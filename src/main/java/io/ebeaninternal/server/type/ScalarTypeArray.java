package io.ebeaninternal.server.type;

/**
 * DB Array types.
 */
public interface ScalarTypeArray {

  /**
   * Return the underlying DB column type.
   */
  String getDbColumnDefn();

}
