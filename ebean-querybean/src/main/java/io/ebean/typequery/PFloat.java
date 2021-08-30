package io.ebean.typequery;

/**
 * Float property.
 *
 * @param <R> the root query bean type
 */
public final class PFloat<R> extends PBaseNumber<R,Float> {

  /**
   * Construct with a property name and root instance.
   *
   * @param name property name
   * @param root the root query bean instance
   */
  public PFloat(String name, R root) {
    super(name, root);
  }

  /**
   * Construct with additional path prefix.
   */
  public PFloat(String name, R root, String prefix) {
    super(name, root, prefix);
  }

}
