package org.tests.types;

/**
 * Encrypted string.
 *
 * @author Roland Praml, FOCONIS AG
 *
 */
public class EncryptedString {

  private final String encryptedData;

  EncryptedString(String encryptedData) {
    this.encryptedData = encryptedData;
  }

  public String getEncryptedData() {
    return encryptedData;
  }

  public String decrypt() {
      return rot13(encryptedData);
  }

  public static EncryptedString encrypt(final String s) {
      return new EncryptedString(rot13(s));
  }

  private static  String rot13(String s) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < s.length(); i++) {
        char c = s.charAt(i);
        if       (c >= 'a' && c <= 'm') c += 13;
        else if  (c >= 'A' && c <= 'M') c += 13;
        else if  (c >= 'n' && c <= 'z') c -= 13;
        else if  (c >= 'N' && c <= 'Z') c -= 13;
       sb.append(c);
    }
    return sb.toString();
  }
}
