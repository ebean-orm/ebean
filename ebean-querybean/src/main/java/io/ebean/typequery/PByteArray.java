package io.ebean.typequery;

/**
 * Byte array property (<code>byte[]</code>).
 *
 * @param <R> the root query bean type
 */
public final class PByteArray<R> extends PBaseValueEqual<R, byte[]> {

  /**
   * Construct with a property name and root instance.
   *
   * @param name property name
   * @param root the root query bean instance
   */
  public PByteArray(String name, R root) {
    super(name, root);
  }

  /**
   * Construct with additional path prefix.
   */
  public PByteArray(String name, R root, String prefix) {
    super(name, root, prefix);
  }

}
