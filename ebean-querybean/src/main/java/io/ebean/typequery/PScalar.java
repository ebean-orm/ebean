package io.ebean.typequery;

/**
 * Property for classes that are serialized/deserialized by
 * ScalarType/AttributeConverter.
 *
 * @param <R> the root query bean type
 * @param <D> the scalar type
 */
public class PScalar<R, D> extends PBaseValueEqual<R, D> {

  /**
   * Construct with a property name and root instance.
   *
   * @param name property name
   * @param root the root query bean instance
   */
  public PScalar(String name, R root) {
    super(name , root);
  }

  /**
   * Construct with additional path prefix.
   */
  public PScalar(String name, R root, String prefix) {
    super(name, root, prefix);
  }

}
