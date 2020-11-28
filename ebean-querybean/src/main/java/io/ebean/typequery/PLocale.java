package io.ebean.typequery;

import java.util.Locale;

/**
 * Locale property.
 *
 * @param <R> the root query bean type
 */
public class PLocale<R> extends PBaseString<R, Locale> {

  /**
   * Construct with a property name and root instance.
   *
   * @param name property name
   * @param root the root query bean instance
   */
  public PLocale(String name, R root) {
    super(name, root);
  }

  /**
   * Construct with additional path prefix.
   */
  public PLocale(String name, R root, String prefix) {
    super(name, root, prefix);
  }

}
