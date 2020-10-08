package io.ebean.typequery;

/**
 * Property for classes that are serialized/deserialized by
 * ScalarType/AttributeConverter. If the classes are comparable,
 * it is assumed that the database can compare the serialized values too.
 *
 * @param <R> the root query bean type
 * @param <D> the scalar type
 */
public class PScalarComparable<R, D extends Comparable<D>> extends PBaseCompareable<R, D> {

  /**
   * Construct with a property name and root instance.
   *
   * @param name property name
   * @param root the root query bean instance
   */
  public PScalarComparable(String name, R root) {
    super(name , root);
  }

  /**
   * Construct with additional path prefix.
   */
  public PScalarComparable(String name, R root, String prefix) {
    super(name, root, prefix);
  }

}
