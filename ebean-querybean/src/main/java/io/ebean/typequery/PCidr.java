package io.ebean.typequery;

import io.ebean.types.Cidr;

/**
 * Cidr property.
 *
 * @param <R> the root query bean type
 */
public class PCidr<R> extends PBaseValueEqual<R, Cidr> {

  /**
   * Construct with a property name and root instance.
   *
   * @param name property name
   * @param root the root query bean instance
   */
  public PCidr(String name, R root) {
    super(name, root);
  }

  /**
   * Construct with additional path prefix.
   */
  public PCidr(String name, R root, String prefix) {
    super(name, root, prefix);
  }
}
