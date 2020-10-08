package io.ebean.typequery;


import org.joda.time.LocalTime;

/**
 * Joda LocalTime property.
 *
 * @param <R> the root query bean type
 */
public class PJodaLocalTime<R> extends PBaseNumber<R,LocalTime> {

  /**
   * Construct with a property name and root instance.
   *
   * @param name property name
   * @param root the root query bean instance
   */
  public PJodaLocalTime(String name, R root) {
    super(name, root);
  }

  /**
   * Construct with additional path prefix.
   */
  public PJodaLocalTime(String name, R root, String prefix) {
    super(name, root, prefix);
  }

}
