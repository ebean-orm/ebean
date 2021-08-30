package io.ebean.typequery;

import java.time.OffsetTime;

/**
 * OffsetTime property.
 *
 * @param <R> the root query bean type
 */
public final class POffsetTime<R> extends PBaseNumber<R,OffsetTime> {

  /**
   * Construct with a property name and root instance.
   *
   * @param name property name
   * @param root the root query bean instance
   */
  public POffsetTime(String name, R root) {
    super(name, root);
  }

  /**
   * Construct with additional path prefix.
   */
  public POffsetTime(String name, R root, String prefix) {
    super(name, root, prefix);
  }

}
