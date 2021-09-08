package org.tests.types;

/**
 * Encrypted string.
 *
 * @author Roland Praml, FOCONIS AG
 *
 */
public class EncryptedBinary {

  private final byte[] encryptedData;

  EncryptedBinary(byte[] encryptedData) {
    this.encryptedData = encryptedData;
  }

  public byte[] getEncryptedData() {
    return encryptedData;
  }

  public byte[] decrypt() {
      return xor(encryptedData);
  }

  public static EncryptedBinary encrypt(final byte[] s) {
      return new EncryptedBinary(xor(s));
  }

  private static  byte[] xor(byte[] s) {
    byte[] ret = new byte[s.length];
    for (int i = 0; i < s.length; i++) {
        ret[i] = (byte) (s[i] ^ i);
    }
    return ret;
  }
}
