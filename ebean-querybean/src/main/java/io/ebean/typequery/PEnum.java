package io.ebean.typequery;

/**
 * BigDecimal property.
 *
 * @param <E> the enum specific type
 * @param <R> the root query bean type
 */
public final class PEnum<R,E> extends PBaseValueEqual<R,E> {

  /**
   * Construct with a property name and root instance.
   *
   * @param name property name
   * @param root the root query bean instance
   */
  public PEnum(String name, R root) {
    super(name, root);
  }

  /**
   * Construct with additional path prefix.
   */
  public PEnum(String name, R root, String prefix) {
    super(name, root, prefix);
  }

}
