package io.ebean.typequery;

import org.joda.time.LocalDateTime;

/**
 * Joda LocalDateTime property.
 *
 * @param <R> the root query bean type
 */
public class PJodaLocalDateTime<R> extends PBaseDate<R,LocalDateTime> {

  /**
   * Construct with a property name and root instance.
   *
   * @param name property name
   * @param root the root query bean instance
   */
  public PJodaLocalDateTime(String name, R root) {
    super(name, root);
  }

  /**
   * Construct with additional path prefix.
   */
  public PJodaLocalDateTime(String name, R root, String prefix) {
    super(name, root, prefix);
  }
}
