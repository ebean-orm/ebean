package io.ebean.typequery;

/**
 * Base property for time types.
 *
 * @param <R> the root query bean type
 * @param <T> the number type
 */
@SuppressWarnings("rawtypes")
public abstract class PBaseTime<R, T extends Comparable> extends PBaseComparable<R, T> {

  /**
   * Construct with a property name and root instance.
   *
   * @param name property name
   * @param root the root query bean instance
   */
  public PBaseTime(String name, R root) {
    super(name, root);
  }

  /**
   * Construct with additional path prefix.
   */
  public PBaseTime(String name, R root, String prefix) {
    super(name, root, prefix);
  }

  /**
   * Same as greater than.
   *
   * @param value the equal to bind value
   * @return the root query bean instance
   */
  public final R after(T value) {
    expr().gt(_name, value);
    return _root;
  }

  /**
   * Same as less than.
   *
   * @param value the equal to bind value
   * @return the root query bean instance
   */
  public final R before(T value) {
    expr().lt(_name, value);
    return _root;
  }
}
