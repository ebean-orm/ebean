package io.ebean.typequery;

/**
 * Boolean property.
 *
 * @param <R> the root query bean type
 */
public final class PBoolean<R> extends PBaseValueEqual<R, Boolean> {

  /**
   * Construct with a property name and root instance.
   *
   * @param name property name
   * @param root the root query bean instance
   */
  public PBoolean(String name, R root) {
    super(name, root);
  }

  /**
   * Construct with additional path prefix.
   */
  public PBoolean(String name, R root, String prefix) {
    super(name, root, prefix);
  }

  /**
   * Is true.
   *
   * @return the root query bean instance
   */
  public R isTrue() {
    expr().eq(_name, Boolean.TRUE);
    return _root;
  }

  /**
   * Is false.
   *
   * @return the root query bean instance
   */
  public R isFalse() {
    expr().eq(_name, Boolean.FALSE);
    return _root;
  }

  /**
   * Is true or false based on the bind value.
   *
   * @param value the equal to bind value
   *
   * @return the root query bean instance
   */
  public R is(boolean value) {
    expr().eq(_name, value);
    return _root;
  }

  /**
   * Is true or false based on the bind value.
   *
   * @param value the equal to bind value
   *
   * @return the root query bean instance
   */
  public R eq(boolean value) {
    expr().eq(_name, value);
    return _root;
  }
}
