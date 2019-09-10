package io.ebeaninternal.server.type;

import io.ebean.config.EncryptKey;
import io.ebean.config.Encryptor;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

/**
 * Simple AES based encryption and decryption.
 */
public class SimpleAesEncryptor implements Encryptor {

  private static final String AES_CIPHER = "AES/CBC/PKCS5Padding";

  private static final String padding = "asldkalsdkadsdfkjsldfjl";

  public SimpleAesEncryptor() {
  }

  private String paddKey(EncryptKey encryptKey) {

    String key = encryptKey.getStringValue();
    int addChars = 16 - key.length();
    if (addChars < 0) {
      return key.substring(0, 16);
    } else if (addChars > 0) {
      return key + padding.substring(0, addChars);
    }
    return key;
  }

  private IvParameterSpec getIvParameterSpec(String initialVector) {
    return new IvParameterSpec(initialVector.getBytes());
  }

  @Override
  public byte[] decrypt(byte[] data, EncryptKey encryptKey) {

    if (data == null) {
      return null;
    }

    String key = paddKey(encryptKey);

    try {

      byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
      IvParameterSpec iv = getIvParameterSpec(key);

      SecretKeySpec sks = new SecretKeySpec(keyBytes, "AES");
      Cipher c = Cipher.getInstance(AES_CIPHER);

      c.init(Cipher.DECRYPT_MODE, sks, iv);

      return c.doFinal(data);

    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public byte[] encrypt(byte[] data, EncryptKey encryptKey) {

    if (data == null) {
      return null;
    }

    String key = paddKey(encryptKey);

    try {
      byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
      IvParameterSpec iv = getIvParameterSpec(key);

      SecretKeySpec sks = new SecretKeySpec(keyBytes, "AES");
      Cipher c = Cipher.getInstance(AES_CIPHER);

      c.init(Cipher.ENCRYPT_MODE, sks, iv);

      return c.doFinal(data);

    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public String decryptString(byte[] data, EncryptKey key) {
    if (data == null) {
      return null;
    }

    byte[] bytes = decrypt(data, key);
    return new String(bytes, StandardCharsets.UTF_8);
  }

  @Override
  public byte[] encryptString(String valueFormatValue, EncryptKey key) {

    if (valueFormatValue == null) {
      return null;
    }
    byte[] d = valueFormatValue.getBytes(StandardCharsets.UTF_8);
    return encrypt(d, key);
  }

}
