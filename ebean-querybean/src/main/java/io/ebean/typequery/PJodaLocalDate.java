package io.ebean.typequery;

import org.joda.time.LocalDate;

/**
 * Joda LocalDate property.
 *
 * @param <R> the root query bean type
 */
public class PJodaLocalDate<R> extends PBaseDate<R,LocalDate> {

  /**
   * Construct with a property name and root instance.
   *
   * @param name property name
   * @param root the root query bean instance
   */
  public PJodaLocalDate(String name, R root) {
    super(name, root);
  }

  /**
   * Construct with additional path prefix.
   */
  public PJodaLocalDate(String name, R root, String prefix) {
    super(name, root, prefix);
  }
}
