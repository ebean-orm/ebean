package io.ebean.typequery;

import org.joda.time.DateMidnight;

/**
 * Joda DateMidnight property.
 *
 * @param <R> the root query bean type
 */
public class PJodaDateMidnight<R> extends PBaseDate<R,DateMidnight> {

  /**
   * Construct with a property name and root instance.
   *
   * @param name property name
   * @param root the root query bean instance
   */
  public PJodaDateMidnight(String name, R root) {
    super(name, root);
  }

  /**
   * Construct with additional path prefix.
   */
  public PJodaDateMidnight(String name, R root, String prefix) {
    super(name, root, prefix);
  }
}
