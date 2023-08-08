package io.ebean.typequery;

import java.time.ZonedDateTime;

/**
 * ZonedDateTime property.
 *
 * @param <R> the root query bean type
 */
public final class PZonedDateTime<R> extends PBaseDate<R, ZonedDateTime> {

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

}
