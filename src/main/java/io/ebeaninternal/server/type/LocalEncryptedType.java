package io.ebeaninternal.server.type;

/**
 * Scalar type that wraps a local/client side encrypted value.
 */
public interface LocalEncryptedType {

  /**
   * Encrypt and return the un-encrypted value.
   */
  Object localEncrypt(Object value);
}
