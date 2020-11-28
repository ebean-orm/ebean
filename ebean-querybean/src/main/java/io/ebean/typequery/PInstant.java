package io.ebean.typequery;


import java.time.Instant;

/**
 * Instant property.
 *
 * @param <R> the root query bean type
 */
public class PInstant<R> extends PBaseDate<R,Instant> {

  /**
   * Construct with a property name and root instance.
   *
   * @param name property name
   * @param root the root query bean instance
   */
  public PInstant(String name, R root) {
    super(name, root);
  }

  /**
   * Construct with additional path prefix.
   */
  public PInstant(String name, R root, String prefix) {
    super(name, root, prefix);
  }

}
