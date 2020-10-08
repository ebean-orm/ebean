package io.ebean.typequery;

import org.joda.time.DateTime;

/**
 * Joda DateTime property.
 *
 * @param <R> the root query bean type
 */
public class PJodaDateTime<R> extends PBaseDate<R,DateTime> {

  /**
   * Construct with a property name and root instance.
   *
   * @param name property name
   * @param root the root query bean instance
   */
  public PJodaDateTime(String name, R root) {
    super(name, root);
  }

  /**
   * Construct with additional path prefix.
   */
  public PJodaDateTime(String name, R root, String prefix) {
    super(name, root, prefix);
  }
}
