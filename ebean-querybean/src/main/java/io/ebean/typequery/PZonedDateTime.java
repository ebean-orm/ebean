package io.ebean.typequery;

import java.time.ZonedDateTime;

/**
 * ZonedDateTime property.
 *
 * @param <R> the root query bean type
 */
public class PZonedDateTime<R> extends PBaseCompareable<R, ZonedDateTime> {

  /**
   * Construct with a property name and root instance.
   *
   * @param name property name
   * @param root the root query bean instance
   */
  public PZonedDateTime(String name, R root) {
    super(name, root);
  }

  /**
   * Construct with additional path prefix.
   */
  public PZonedDateTime(String name, R root, String prefix) {
    super(name, root, prefix);
  }

  /**
   * Same as greater than.
   *
   * @param value the equal to bind value
   * @return the root query bean instance
   */
  public R after(ZonedDateTime value) {
    expr().gt(_name, value);
    return _root;
  }

  /**
   * Same as less than.
   *
   * @param value the equal to bind value
   * @return the root query bean instance
   */
  public R before(ZonedDateTime value) {
    expr().lt(_name, value);
    return _root;
  }
}
