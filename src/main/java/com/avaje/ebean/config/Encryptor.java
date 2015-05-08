package com.avaje.ebean.config;

/**
 * Used for Java side encryption of properties when DB encryption is not used.
 * <p>
 * By default this is used on non-varchar types such as Blobs.
 * </p>
 * 
 * @author rbygrave
 * 
 */
public interface Encryptor {

  /**
   * Encrypt the data using the key.
   */
  byte[] encrypt(byte[] data, EncryptKey key);

  /**
   * Decrypt the data using the key.
   */
  byte[] decrypt(byte[] data, EncryptKey key);

  /**
   * Encrypt the formatted string value using a key.
   */
  byte[] encryptString(String formattedValue, EncryptKey key);

  /**
   * Decrypt the data returning a formatted string value using a key.
   */
  String decryptString(byte[] data, EncryptKey key);

}
