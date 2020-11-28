package io.ebean.typequery;

/**
 * Class property.
 *
 * @param <R> the root query bean type
 */
public class PClass<R> extends PBaseString<R,Class> {

  /**
   * Construct with a property name and root instance.
   *
   * @param name property name
   * @param root the root query bean instance
   */
  public PClass(String name, R root) {
    super(name, root);
  }

  /**
   * Construct with additional path prefix.
   */
  public PClass(String name, R root, String prefix) {
    super(name, root, prefix);
  }

}
