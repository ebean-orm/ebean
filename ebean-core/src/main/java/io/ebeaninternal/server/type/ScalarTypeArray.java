package io.ebeaninternal.server.type;

/**
 * DB Array types.
 */
public interface ScalarTypeArray {

  /**
   * Return the underlying DB column type.
   */
  String getDbColumnDefn();

  /**
   * Return the Java type of the individual array elements.
   * <p>
   * Used to bind the array correctly when the collection value is empty
   * and so the element type can't be determined from the collection content.
   */
  Class<?> elementType();

}
